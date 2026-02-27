package com.nexushardware.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nexushardware.app.data.model.CarritoItem
import java.util.Date
import com.nexushardware.app.data.model.Producto

//Mi db del proyecto
class NexusBDHelper(context: Context): SQLiteOpenHelper(context, "NexusHardware.db",null, 1) {
    //Exepcion personalizada
    class StockInsuficienteException(message: String) : Exception(message)
    companion object {
        const val ESTADO_PENDIENTE = 0 // Producto en el carrito
        const val ESTADO_SINCRONIZADO = 1 // Producto ya comprado
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val crearTablaUsuarios = """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE,
                password_hash TEXT, 
                nombre_completo TEXT,
                telefono TEXT,
                es_admin INTEGER DEFAULT 0
            )
        """.trimIndent()
        db?.execSQL(crearTablaUsuarios)

        val crearTablaCategorias = """
            CREATE TABLE categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE
            )
        """.trimIndent()
        db?.execSQL(crearTablaCategorias)

        val crearTablaProductos = """
            CREATE TABLE productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                descripcion TEXT,
                precio REAL,
                stock INTEGER CHECK(stock >= 0),--el CK evita stock negativo desde el motor de db
                categoria TEXT,
                url_imagen TEXT
            )
        """.trimIndent()
        db?.execSQL(crearTablaProductos)

        val crearTablaCarrito = """
            CREATE TABLE carrito (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER,
                producto_id INTEGER,
                cantidad INTEGER,
                fecha_agregado TEXT,
                estado_sync INTEGER DEFAULT $ESTADO_PENDIENTE,
                FOREIGN KEY(usuario_id) REFERENCES usuarios(id),
                FOREIGN KEY(producto_id) REFERENCES productos(id)
            )
        """.trimIndent()
        db?.execSQL(crearTablaCarrito)

        insertarDatosPrueba(db)
    }

    private fun insertarDatosPrueba(db: SQLiteDatabase?) {
        //inserto datos de prueba
        db?.execSQL("INSERT INTO usuarios (email, password_hash, nombre_completo, es_admin) VALUES ('admin@nexus.pe', '123456', 'Admin Nexus', 1)")
        //datos de prueba
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('GPU')")
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('CPU')")
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('Perifericos')")
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('Monitores')")
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('Almacenamiento')")
        db?.execSQL("INSERT INTO categorias (nombre) VALUES ('Laptops')")

        // se agregan la url para usar imagenes por glide
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock, url_imagen) VALUES ('NVIDIA RTX 4090', 'Tarjeta gráfica de última generación 24GB. Máximo rendimiento para gaming en 4K y renderizado 3D.', 8500.00, 'GPU', 5, 'https://m.media-amazon.com/images/I/815d7TTP5UL.jpg')")
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock, url_imagen) VALUES ('Intel Core i9 14900K', 'Procesador 24 núcleos desbloqueado. Frecuencia turbo máxima de 6.0 GHz.', 2800.00, 'CPU', 10, 'https://m.media-amazon.com/images/I/51ZKpp9PV0L.jpg')")
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock, url_imagen) VALUES ('Teclado Mecánico RGB', 'Switch Cherry MX Blue, chasis de aluminio y reposamuñecas.', 450.00, 'Perifericos', 20, 'https://m.media-amazon.com/images/I/71+zaeIJx0L._AC_UF894,1000_QL80_.jpg')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // En desarrollo, si cambias la BD, borramos todo y recreamos
        db?.execSQL("DROP TABLE IF EXISTS carrito")
        db?.execSQL("DROP TABLE IF EXISTS productos")
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        db?.execSQL("DROP TABLE IF EXISTS categorias")
        onCreate(db)
    }

    //DAOs

    //login
    fun validarLogin(email: String, pass: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE email=? AND password_hash=?", arrayOf(email, pass))
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    //clientes
    fun registrarUsuario(nombre: String, email: String, pass: String): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put("nombre_completo", nombre)
            put("email", email)
            put("password_hash", pass)
        }
        return db.insert("usuarios", null, valores)
    }

    //función mejorada con validación de stock en tiempo real
    fun agregarAlCarrito(usuarioId: Int, productoId: Int, cantidad: Int): Long {
        val db = this.writableDatabase

        //consultamos el stock real del producto
        val cursorStock = db.rawQuery("SELECT stock, nombre FROM productos WHERE id=?", arrayOf(productoId.toString()))
        var stockDisponible = 0
        var nombreProducto = ""

        if (cursorStock.moveToFirst()) {
            stockDisponible = cursorStock.getInt(0)
            nombreProducto = cursorStock.getString(1)
        }
        cursorStock.close()

        //verificamos cuánto tiene ya este usuario de este producto en el carrito
        val sqlConsulta = "SELECT id, cantidad FROM carrito WHERE usuario_id=? AND producto_id=? AND estado_sync=?"
        val cursorCarrito = db.rawQuery(sqlConsulta, arrayOf(usuarioId.toString(), productoId.toString(), ESTADO_PENDIENTE.toString()))

        var cantidadActualEnCarrito = 0
        var idCarritoExistente = -1

        if (cursorCarrito.moveToFirst()) {
            idCarritoExistente = cursorCarrito.getInt(0)
            cantidadActualEnCarrito = cursorCarrito.getInt(1)
        }
        cursorCarrito.close()

        //IMPORTANTEE, VALIDAMOS SI SUPERA EL LÍMITE
        val cantidadTotalDeseada = cantidadActualEnCarrito + cantidad

        if (cantidadTotalDeseada > stockDisponible) {
            //lanzamos la excepción para que la vista la atrape
            throw StockInsuficienteException("Solo quedan $stockDisponible unidades de $nombreProducto.")
        }

        //si tod0 está bien, procedemos a insertar o actualizar
        val resultado: Long
        if (idCarritoExistente != -1) {
            //actualizamos sumando la cantidad
            val values = ContentValues().apply { put("cantidad", cantidadTotalDeseada) }
            resultado = db.update("carrito", values, "id=?", arrayOf(idCarritoExistente.toString())).toLong()
        } else {
            //insertamos como nuevo item
            val values = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("producto_id", productoId)
                put("cantidad", cantidad)
                put("fecha_agregado", Date().toString())
                put("estado_sync", ESTADO_PENDIENTE)
            }
            resultado = db.insert("carrito", null, values)
        }
        return resultado
    }

    fun obtenerCarrito(usuarioId: Int): List<CarritoItem> {
        val lista = mutableListOf<CarritoItem>()
        val db = this.readableDatabase

        // query para obtener soloo la cantidad del carrito Y el nombre/precio del producto
        //solo traemos los que tengan estado_sync = 0 (Pendientes de compra)
        val sql = """
            SELECT c.id, p.id, p.nombre, p.precio, c.cantidad, p.url_imagen 
            FROM carrito c 
            INNER JOIN productos p ON c.producto_id = p.id 
            WHERE c.usuario_id = ? AND c.estado_sync = $ESTADO_PENDIENTE
        """

        val cursor = db.rawQuery(sql, arrayOf(usuarioId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val idCarrito = cursor.getInt(0)
                val idProducto = cursor.getInt(1)
                val nombre = cursor.getString(2)
                val precio = cursor.getDouble(3)
                val cantidad = cursor.getInt(4)
                val imagen = cursor.getString(5) ?: ""

                lista.add(CarritoItem(idCarrito, idProducto, nombre, precio, cantidad, imagen))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    //Función para borrar un item del carrito
    fun eliminarItemCarrito(idCarrito: Int): Int {
        val db = this.writableDatabase
        return db.delete("carrito", "id=?", arrayOf(idCarrito.toString()))
    }

    //nueva funcion para procesar la compra
    fun procesarCompra(usuarioId: Int): Int {
        val db = this.writableDatabase
        var productosDistintosProcesados = 0

        // INICIO DE TRANSACCIÓNt0do ocurre o nada ocurre.
        db.beginTransaction()
        try {
            //fase 1 de Lectura y validación
            // Unimos carrito con productos para ver el stock real actual
            val sqlConsulta = """
                SELECT c.producto_id, c.cantidad, p.stock, p.nombre 
                FROM carrito c 
                INNER JOIN productos p ON c.producto_id = p.id 
                WHERE c.usuario_id = ? AND c.estado_sync = $ESTADO_PENDIENTE
            """.trimIndent()

            val cursor = db.rawQuery(sqlConsulta, arrayOf(usuarioId.toString()))
            val listaAValidar = mutableListOf<Triple<Int, Int, String>>()

            if (cursor.moveToFirst()) {
                do {
                    val idProd = cursor.getInt(0)
                    val cantPedida = cursor.getInt(1)
                    val stockReal = cursor.getInt(2)
                    val nombreProd = cursor.getString(3)

                    //si un solo producto falla, lanzamos la excepción y se cancela tod0
                    if (cantPedida > stockReal) {
                        throw StockInsuficienteException("No hay suficiente stock de: $nombreProd (Disponible: $stockReal)")
                    }

                    // Guardamos en memoria para actualizar stock
                    listaAValidar.add(Triple(idProd, cantPedida, nombreProd))
                } while (cursor.moveToNext())
            }
            cursor.close()

            if (listaAValidar.isEmpty()) return 0

            //fase 2 de escrituraaa
            //solo llegamos aquí si la fase 1 fue exitosa para TODOS los productos.
            for (item in listaAValidar) {
                val id = item.first
                val cantidad = item.second

                //UPDATE con triple seguridad: ID + Stock suficiente en la misma sentencia
                val sqlUpdateStock = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?"
                db.execSQL(sqlUpdateStock, arrayOf(cantidad, id, cantidad))

                productosDistintosProcesados++
            }

            //fase 3 para cerra el carrito
            //cambiamos el estado a comprado/sincronizado
            val values = ContentValues().apply {
                put("estado_sync", ESTADO_SINCRONIZADO)
            }
            db.update("carrito", values, "usuario_id=? AND estado_sync=?",
                arrayOf(usuarioId.toString(), ESTADO_PENDIENTE.toString()))

            //si se llega aquí sin errores, confirmamos los cambios físicamente.
            db.setTransactionSuccessful()

        } catch (e: StockInsuficienteException) {
            //Relanzamos la excepción para que la UI la atrape y muestre el mensaje
            throw e
        } catch (e: Exception) {
            //error genérico
            productosDistintosProcesados = 0
        } finally {
            //finaliza la burbuja de seguridad/ hace Rollback si no se llamó a setTransactionSuccessful
            db.endTransaction()
        }
        return productosDistintosProcesados
    }

    //MODULO ADMIN: MANTINIMIENTO DE PRODYCTOS

    fun agregarProducto(nombre: String, desc: String, precio: Double, stock: Int, cat: String, url: String): Long {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put("nombre", nombre)
            put("descripcion", desc)
            put("precio", precio)
            put("stock", stock)
            put("categoria", cat)
            put("url_imagen", url)
        }
        val resultado = db.insert("productos", null, values)
        return resultado
    }

    //funcionar apara obtener las categoría y llenarlas en el spinner
    fun obtenerCategorias(): List<String> {
        val lista = mutableListOf<String>()
        val db = this.readableDatabase

        //ordenado alfabeticamente
        val cursor = db.rawQuery("SELECT nombre FROM categorias ORDER BY nombre ASC", null)

        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    //Función para buscar productos filtrando dinamicamente
    /*fun buscarProductos(query: String): List<Producto> {
        val lista = mutableListOf<Producto>()
        val db = this.readableDatabase

        //buscamos tanto en el nombre como en la categoría
        val sql = "SELECT * FROM productos WHERE nombre LIKE ? OR categoria LIKE ?"
        val parametro = "%$query%"

        val cursor = db.rawQuery(sql, arrayOf(parametro, parametro))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val descripcion = cursor.getString(2)
                val precio = cursor.getDouble(3)
                val stock = cursor.getInt(4)
                val categoria = cursor.getString(5)
                val url = cursor.getString(6) ?: ""

                //lista.add(Producto(id, nombre, descripcion, precio, stock, categoria, url)) --falta ajustar
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }*/

    //faltan agregar mas funciones
}