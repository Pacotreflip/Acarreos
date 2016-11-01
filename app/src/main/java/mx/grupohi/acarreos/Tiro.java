package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Tiro {

    private Integer idTiro;
    public String descripcion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Tiro(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("tiros", null, data) > -1;
        if (result) {
            this.idTiro = Integer.valueOf(data.getAsString("idtiro"));
            this.descripcion = data.getAsString("descripcion");
        }
        return result;
    }

    public Tiro find(Integer idTiro){
        Cursor c= db.rawQuery("SELECT * FROM tiros WHERE idtiro = '" + idTiro + "'", null);
        if( c != null && c.moveToFirst()){
            this.idTiro=c.getInt(0);
            this.descripcion=c.getString(1);
            return this;
        }
        else{
            return null;
        }

    }

    ArrayList<String> getArrayListDescripciones(Integer idOrigen) {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM tiros WHERE idtiro IN (SELECT idtiro FROM rutas WHERE idorigen = '" + idOrigen + "') ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("descripcion")));
                    }
                }
            } finally {
                c.close();
            }
        return data;
    }

    ArrayList<String> getArrayListId(Integer idOrigen) {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM tiros WHERE idtiro IN (SELECT idtiro FROM rutas WHERE idorigen = '" + idOrigen + "') ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("idtiro")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("idtiro")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("idtiro")));
                    }
                }
            } finally {
                c.close();
            }
        return data;
    }
}
