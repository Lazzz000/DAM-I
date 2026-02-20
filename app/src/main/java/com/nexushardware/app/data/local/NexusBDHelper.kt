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

    companion object {
        const val ESTADO_PENDIENTE = 0
        const val ESTADO_SINCRONIZADO = 1
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
                stock INTEGER,
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

    //funcion mejorada porque verifica si el usuario ya tiene en el carrito mas unidades del mismo item
    fun agregarAlCarrito(usuarioId: Int, productoId: Int, cantidad: Int): Long {
        val db = this.writableDatabase

        // Validamo si ya existe este producto en el carrito de un usuario
        val sqlConsulta = "SELECT id, cantidad FROM carrito WHERE usuario_id=? AND producto_id=? AND estado_sync=?"
        val cursor = db.rawQuery(sqlConsulta, arrayOf(usuarioId.toString(), productoId.toString(), ESTADO_PENDIENTE.toString()))

        val resultado: Long

        if (cursor.moveToFirst()) {
            // Si existe hacemos un update acumulando la cantidad
            val idCarritoExistente = cursor.getInt(0)
            val cantidadActual = cursor.getInt(1)

            val values = ContentValues().apply {
                put("cantidad", cantidadActual + cantidad)
            }
            // actualizamos esa fila especifica
            resultado = db.update("carrito", values, "id=?", arrayOf(idCarritoExistente.toString())).toLong()
        } else {
            //si no existe, hacemos el insert normal
            val values = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("producto_id", productoId)
                put("cantidad", cantidad)
                put("fecha_agregado", Date().toString())
                put("estado_sync", ESTADO_PENDIENTE)
            }
            resultado = db.insert("carrito", null, values)
        }
        cursor.close()
        db.close()
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

        val values = ContentValues().apply {
            put("estado_sync", ESTADO_SINCRONIZADO)
        }

        // aqui se actualiza todos los registros pendientes del usuario
        val filasActualizadas = db.update(
            "carrito",
            values,
            "usuario_id=? AND estado_sync=?",
            arrayOf(usuarioId.toString(), ESTADO_PENDIENTE.toString())
        )

        db.close()
        return filasActualizadas// devuelve cuantos productos se compraron
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
        db.close()
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
    fun buscarProductos(query: String): List<Producto> {
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

                lista.add(Producto(id, nombre, descripcion, precio, stock, categoria, url))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    //faltan agregar mas funciones
}