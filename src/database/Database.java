package database;

import java.util.ArrayList;

public class Database {
    private ArrayList<Schema> schemas;

    Database(){
        this.schemas = new ArrayList<>();
    }

    public ArrayList<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(ArrayList<Schema> schemas) {
        this.schemas = schemas;
    }
}
