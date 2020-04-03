package database;

import java.util.ArrayList;

public class Table {
    private String name;
    private ArrayList<Column> columns;

    Table(){
        this.columns = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }
}
