package database;

import java.util.List;

/**
 * Database instance contains {@code List}
 * of schemas in database
 *
 * @author Arina Fedorovskaya
 */

public class Database {
    private List<Schema> schemas;

    public List<Schema> getSchemas() {
        return schemas;
    }

    public Schema getSchemaByName(String name){
        for (Schema schema: this.schemas) {
            if(schema.getName().equals(name)){
                return schema;
            }
        }

        return null;
    }

}
