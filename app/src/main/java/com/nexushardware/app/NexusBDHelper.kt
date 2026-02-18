package com.nexushardware.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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

        // Productos de ejemplo para que la lista no salga vacía
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock) VALUES ('GeForce RTX 4060', 'MSI Ventus 2X Black OC', 320.00, 'GPU', 10)")
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock) VALUES ('Ryzen 5 7600X', 'Procesador 6 núcleos AM5', 249.00, 'CPU', 15)")
        db?.execSQL("INSERT INTO productos (nombre, descripcion, precio, categoria, stock) VALUES ('Logitech G502 HERO', 'Mouse Gamer Alta Precisión', 45.00, 'Perifericos', 30)")
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
        val valores = android.content.ContentValues().apply {
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

            val values = android.content.ContentValues().apply {
                put("cantidad", cantidadActual + cantidad)
            }
            // actualizamos esa fila especifica
            resultado = db.update("carrito", values, "id=?", arrayOf(idCarritoExistente.toString())).toLong()
        } else {
            //si no existe, hacemos el insert normal
            val values = android.content.ContentValues().apply {
                put("usuario_id", usuarioId)
                put("producto_id", productoId)
                put("cantidad", cantidad)
                put("fecha_agregado", java.util.Date().toString())
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

}