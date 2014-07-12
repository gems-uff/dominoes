package dao;

public class FactoryDao {
    
    /**
     * This function is called to define the type of access to database
     * @param type String with access type
     * @return Access type
     */
    public static DominoesDao getDominoesDao(String type){
        if (type.toUpperCase().equals("TXT")) {
            return new DominoesTXTDao();
        }else if (type.toUpperCase().equals("SQL")) {
            return new DominoesSQLDao();
        }
        return null;
    }
}
