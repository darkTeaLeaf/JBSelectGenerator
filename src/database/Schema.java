package database;

import java.util.ArrayList;

public class Schema {
    private String name;
    private ArrayList<Table> tables;

    Schema(){
        this.tables = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Table> getSchemas() {
        return tables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchemas(ArrayList<Table> tables) {
        this.tables = tables;
    }
}
