package dao;

import domain.Dominoes;
import java.io.IOException;
import java.util.ArrayList;

public class DominoesSQLDao implements DominoesDao{

    @Override
    public ArrayList<Dominoes> loadAllMatrices() throws IOException {
        return null;
    }

    @Override
    public Dominoes loadMatrix(Dominoes dominoes) throws IOException {
        return null;
    }

    @Override
    public boolean removeMatrix(Dominoes dominoes) throws IOException {
        return true;
    }

    @Override
    public boolean saveMatrix(Dominoes dominoes) throws IOException {
        return true;
    }
    
}
