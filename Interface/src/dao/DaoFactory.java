package dao;

public class DaoFactory {
    
	public static final String TYPE_SQL = "SQL";
	
    /**
     * This function is called to define the type of access to database
     * @param type String with access type
     * @return Access type
     */
    public static DominoesDao getDominoesDao(String type){
        if (type.toUpperCase().equals(DaoFactory.TYPE_SQL)) {
            return new DominoesSQLDao();
        }
        return null;
    }
}
