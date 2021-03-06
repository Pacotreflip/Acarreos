package mx.grupohi.acarreos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import java.util.GregorianCalendar;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */
 class DBScaSqlite extends SQLiteOpenHelper {


    DBScaSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static String[] queries = new String[] {
            "CREATE TABLE user (idusuario INTEGER, idproyecto INTEGER, nombre, TEXT, base_datos TEXT, descripcion_database TEXT, user TEXT, pass TEXT)",
            "CREATE TABLE camiones (idcamion INTEGER, placas TEXT, marca TEXT, modelo TEXT, ancho REAL, largo REAL, alto REAL, economico TEXT, capacidad INTEGER, numero_viajes INTEGER)",
            "CREATE TABLE tiros (idtiro INTEGER, descripcion TEXT)",
            "CREATE TABLE origenes (idorigen INTEGER, descripcion TEXT, estado INTEGER)",
            "CREATE TABLE rutas (idruta INTEGER, clave TEXT, idorigen INTEGER, idtiro INTEGER, totalkm TEXT)",
            "CREATE TABLE materiales (idmaterial INTEGER, descripcion TEXT)",
            "CREATE TABLE tags (uid TEXT, idcamion INTEGER, idproyecto INTEGER)",
            "CREATE TABLE viajesnetos (ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "FechaCarga VARCHAR(8),"+
                    "HoraCarga VARCHAR(8),"+
                    "IdProyecto INTEGER,"+
                    "IdCamion INTEGER,"+
                    "IdOrigen INTEGER,"+
                    "FechaSalida VARCHAR(8),"+
                    "HoraSalida VARCHAR(8),"+
                    "IdTiro INTEGER,"+
                    "FechaLlegada VARCHAR(8),"+
                    "HoraLlegada VARCHAR(8),"+
                    "IdMaterial INTEGER,"+
                    "Observaciones TEXT,"+
                    "Creo TEXT,"+
                    "Estatus INTEGER, " +
                    "Ruta INTEGER, " +
                    "Code TEXT, uidTAG TEXT);",
            "CREATE TABLE coordenadas (IMEI TEXT, idevento INT, latitud TEXT, longitud TEXT, fecha_hora TEXT, code TEXT)",
            "CREATE TABLE camion_tag (ID INTEGER PRIMARY KEY AUTOINCREMENT, IMEI TEXT, id_camion INT, id_tags TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);"
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query: queries){
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS origenes");
        db.execSQL("DROP TABLE IF EXISTS rutas");
        db.execSQL("DROP TABLE IF EXISTS materiales");
        db.execSQL("DROP TABLE IF EXISTS tiros");
        db.execSQL("DROP TABLE IF EXISTS tags");
        db.execSQL("DROP TABLE IF EXISTS viajesnetos");
        db.execSQL("DROP TABLE IF EXISTS coordenadas");
        db.execSQL("DROP TABLE IF EXISTS camion_tag");

        for (String query: queries){
            db.execSQL(query);
        }

        db.close();
    }

    void deleteCatalogos() {
        SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM user");
        db.execSQL("DELETE FROM camiones");
        db.execSQL("DELETE FROM tiros");
        db.execSQL("DELETE FROM origenes");
        db.execSQL("DELETE FROM rutas");
        db.execSQL("DELETE FROM materiales");
        db.execSQL("DELETE FROM tags");

        db.close();
    }
}
