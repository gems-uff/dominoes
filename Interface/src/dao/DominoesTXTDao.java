package dao;

import domain.Dominoes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.imageio.IIOException;

public class DominoesTXTDao implements DominoesDao {

    private String path = "db";

    public DominoesTXTDao() {
        if (!new File(path).exists()) {
            new File(path).mkdir();
        }
    }

    @Override
    public ArrayList<Dominoes> loadAllMatrices() throws IOException, SQLException {
        String separator = "/";
        String matrixSeparator = ",";

        ArrayList<Dominoes> result = new ArrayList<>();
        String[] auxMatrix = null;
        String[] auxLine = null;
        String[] auxHistoric = null;

        int type = -1;
        String idRow = null, idCol = null;
        int width = 0;
        int height = 0;
        ArrayList<String> historic = null;
        byte[][] mat = null;

        File file = new File(path + "/Dominoes.txt");
        if (!file.exists()) {
            throw new IOException("not exist the file \"" + file.getAbsoluteFile() + "\"");
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();

        do {
            if (line == null) {
                throw new IOException("Error to read the file");
            }
            auxLine = line.split(separator);

            if (auxLine.length != Dominoes.INDEX_SIZE) {
                throw new IOException("Invalid data");
            }
            type = Integer.parseInt(auxLine[Dominoes.INDEX_TYPE]);

            idRow = auxLine[Dominoes.INDEX_ID_ROW];
            idCol = auxLine[Dominoes.INDEX_ID_COL];

            height = Integer.parseInt(auxLine[Dominoes.INDEX_HEIGHT]);
            width = Integer.parseInt(auxLine[Dominoes.INDEX_WIDTH]);

            // acrescentar o campo Histórico em todos os tipo de acesso ao arquivos.
            auxHistoric = auxLine[Dominoes.INDEX_HIST].split(matrixSeparator);

            historic = new ArrayList<>();

            for (int i = 0; i < auxHistoric.length; i++) {
                historic.add(auxHistoric[i]);

            }

            mat = new byte[height][width];
            auxMatrix = auxLine[Dominoes.INDEX_MATRIX].split(matrixSeparator);

            if (auxMatrix.length != height * width) {
                throw new IOException("Invalid data");
            }

            int count = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    mat[i][j] = Byte.parseByte(auxMatrix[count++]);
                }
            }
            Dominoes domino;
            if (type == Dominoes.TYPE_BASIC) {
                domino = null;//new Dominoes(idRow, idCol, mat);
            } else {
                domino = null;///new Dominoes(type, idRow, idCol, historic, mat);
            }
            result.add(domino);

            line = br.readLine();

        } while (line != null);
        br.close();

        return result;
    }

    @Override
    public Dominoes loadMatrix(Dominoes domino) throws IOException {
        String separator = "/";
        String matrixSeparator = ",";

        Dominoes result = null;
        String[] auxMatrix = null;
        String[] auxLine = null;
        String[] auxHistoric = null;

        int type = -1;
        String idRow = null, idCol = null;
        int width = 0;
        int height = 0;
        ArrayList<String> historic = null;
        byte[][] mat = null;

        File file = new File(path + "/Dominoes.txt");
        if (!file.exists()) {
            throw new IIOException("not exist the file in directory: \"" + file.getAbsoluteFile() + "\"");
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();

        do {
            if (line == null) {
                throw new IOException("Error to read the file");
            }
            auxLine = line.split(separator);

            if (auxLine.length != Dominoes.INDEX_SIZE) {
                throw new IOException("Invalid data");
            }

            idRow = auxLine[Dominoes.INDEX_ID_ROW];
            idCol = auxLine[Dominoes.INDEX_ID_COL];

            if (idRow.trim().equals(domino.getIdRow()) && idCol.trim().equals(domino.getIdCol())) {
                continue;
            }

            type = Integer.parseInt(auxLine[Dominoes.INDEX_TYPE]);

            auxHistoric = auxLine[Dominoes.INDEX_HIST].split(matrixSeparator);
            historic = new ArrayList<>();
            for (int i = 0; i < auxHistoric.length; i++) {
                historic.add(auxHistoric[i]);

            }

            height = Integer.parseInt(auxLine[Dominoes.INDEX_HEIGHT]);
            width = Integer.parseInt(auxLine[Dominoes.INDEX_WIDTH]);

            mat = new byte[height][width];
            auxMatrix = auxLine[Dominoes.INDEX_MATRIX].split(matrixSeparator);

            if (auxMatrix.length != height * width) {
                throw new IOException("Invalid data");
            }

            int count = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    mat[i][j] = Byte.parseByte(auxMatrix[count++]);
                }
            }

            result = null;//new Dominoes(type, idRow, idCol, historic, mat);

            line = br.readLine();
            break;

        } while (line != null);
        br.close();

        return result;
    }

    @Override
    public boolean removeMatrix(Dominoes domino) throws IOException {

        String separator = "/";
        String matrixSeparator = ",";

        String[] auxMatrix = null;
        String[] auxLine = null;
        String[] auxHistoric = null;

        int type = -1;
        String idRow = null, idCol = null;
        int width = 0;
        int height = 0;
        ArrayList<String> historic = null;
        byte[][] mat = null;

        File file = new File(path + "/Dominoes.txt");
        File fileTemp = new File(path + "/~Dominoes.txt");

        try {
            fileTemp.createNewFile();

            if (!file.exists()) {
                fileTemp.delete();
                throw new IOException("not exist the file \"" + file.getAbsoluteFile() + "\"");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileTemp));
            String line = br.readLine();

            do {
                if (line == null) {
                    fileTemp.delete();
                    throw new IOException("Error to read the file");
                }
                auxLine = line.split(separator);

                if (auxLine.length != Dominoes.INDEX_SIZE) {
                    fileTemp.delete();
                    throw new IOException("Invalid data");
                }

                idRow = auxLine[Dominoes.INDEX_ID_ROW];
                idCol = auxLine[Dominoes.INDEX_ID_COL];

                // if not is the element
                if (!(idRow.trim().equals(domino.getIdRow()) && idCol.trim().equals(domino.getIdCol()))) {
                    type = Integer.parseInt(auxLine[Dominoes.INDEX_TYPE]);

                    auxHistoric = auxLine[Dominoes.INDEX_HIST].split(matrixSeparator);
                    historic = new ArrayList<>();
                    for (int i = 0; i < auxHistoric.length; i++) {
                        historic.add(auxHistoric[i]);

                    }

                    height = Integer.parseInt(auxLine[Dominoes.INDEX_HEIGHT]);
                    width = Integer.parseInt(auxLine[Dominoes.INDEX_WIDTH]);

                    mat = new byte[height][width];
                    auxMatrix = auxLine[Dominoes.INDEX_MATRIX].split(matrixSeparator);

                    if (auxMatrix.length != height * width) {
                        fileTemp.delete();
                        throw new IOException("Invalid data");
                    }

                    bw.write(type
                            + separator
                            + idRow
                            + separator
                            + idCol
                            + separator
                            + height
                            + separator
                            + width
                            + separator);

                    for (int i = 0; i < historic.size(); i++) {
                        bw.write(historic.get(i));
                        if (i < historic.size() - 1) {
                            bw.write(matrixSeparator);

                        }
                    }

                    bw.write(separator);

                    int count = 0;
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            mat[i][j] = Byte.parseByte(auxMatrix[count++]);
                            bw.write(String.valueOf((int) mat[i][j]));
                            //if (i < domino.getHeight() - 1 || j < domino.getWidth() - 1) {
                              //  bw.write(matrixSeparator);

                            //}
                        }
                    }
                    bw.write("\n");

                }
                line = br.readLine();

            } while (line != null);

            br.close();
            bw.close();

            br = new BufferedReader(new FileReader(fileTemp));
            bw = new BufferedWriter(new FileWriter(file));
            line = br.readLine();

            while (line != null) {
                bw.write(line + "\n");
                line = br.readLine();
            }

            br.close();
            bw.close();

            fileTemp.delete();

            return true;

        } catch (IOException ex) {

            throw new IOException(ex.getMessage());
        } finally {
            fileTemp.delete();
        }
    }

    @Override
    public boolean saveMatrix(Dominoes domino) throws IOException {
        String separator = "/";
        String matrixSeparator = ",";

        String[] auxMatrix = null;
        String[] auxLine = null;
        String[] auxHistoric = null;

        int type = -1;
        String idRow = null, idCol = null;
        int width = 0;
        int height = 0;
        int count = 0;
        ArrayList<String> historic = null;
        byte[][] mat = null;

        boolean isWrited = false;
        boolean exist = false;

        File file = new File(path + "/Dominoes.txt");
        File fileTemp = new File(path + "/~Dominoes.txt");

        try {
            fileTemp.createNewFile();

            if (!file.exists()) {
                fileTemp.delete();
                throw new IOException("not exist the file \"" + file.getAbsoluteFile() + "\"");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileTemp));
            String line = br.readLine();

            do {
                if (line == null) {
                    break;
                }
                auxLine = line.split(separator);

                if (auxLine.length != Dominoes.INDEX_SIZE) {
                    fileTemp.delete();
                    throw new IOException("Invalid data");
                }

                idRow = auxLine[Dominoes.INDEX_ID_ROW];
                idCol = auxLine[Dominoes.INDEX_ID_COL];

                if (idRow.compareTo(domino.getIdRow()) == 0
                        && idCol.compareTo(domino.getIdCol()) == 0) {
                    exist = true;
                }
                if (!exist
                        && idRow.compareTo(domino.getIdRow()) > 0
                        && idCol.compareTo(domino.getIdCol()) > 0) {
                    isWrited = true;

                    type = domino.getType();

                    idRow = domino.getIdRow();
                    idCol = domino.getIdCol();

                    historic = domino.getHistoric();

                    height = 0;//domino.getHeight();
                    width = 0;//domino.getWidth();

                    //mat = domino.getMat();

                    System.out.println(mat.length);

                    if (mat.length * mat[0].length != height * width) {
                        fileTemp.delete();
                        throw new IOException("Invalid data");
                    }

                    bw.write(type
                            + separator
                            + idRow
                            + separator
                            + idCol
                            + separator
                            + height
                            + separator
                            + width
                            + separator);
                    for (int i = 0; i < historic.size(); i++) {
                        bw.write(historic.get(i));
                        if (i < historic.size() - 1) {
                            bw.write(matrixSeparator);
                        }
                    }

                    bw.write(separator);

                    count = 0;
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            mat[i][j] = Byte.parseByte(auxMatrix[count++]);
                            bw.write(String.valueOf((int) mat[i][j]));
                           // if (i < domino.getHeight() - 1 || j < domino.getWidth() - 1) {
                             //   bw.write(matrixSeparator);
                            //}
                        }
                    }
                    bw.write("\n");
                }

                idRow = auxLine[Dominoes.INDEX_ID_ROW];
                idCol = auxLine[Dominoes.INDEX_ID_COL];

                type = Integer.parseInt(auxLine[Dominoes.INDEX_TYPE]);

                auxHistoric = auxLine[Dominoes.INDEX_HIST].split(matrixSeparator);
                historic = new ArrayList<>();
                for (int i = 0; i < auxHistoric.length; i++) {
                    historic.add(auxHistoric[i]);

                }

                height = Integer.parseInt(auxLine[Dominoes.INDEX_HEIGHT]);
                width = Integer.parseInt(auxLine[Dominoes.INDEX_WIDTH]);

                mat = new byte[height][width];
                auxMatrix = auxLine[Dominoes.INDEX_MATRIX].split(matrixSeparator);

                count = 0;
                for (int i = 0; i < mat.length; i++) {
                    for (int j = 0; j < mat[0].length; j++) {
                        mat[i][j] = (byte) Integer.parseInt(auxMatrix[count++]);
                    }
                }

                bw.write(type
                        + separator
                        + idRow
                        + separator
                        + idCol
                        + separator
                        + height
                        + separator
                        + width
                        + separator);

                for (int i = 0; i < historic.size(); i++) {
                    bw.write(historic.get(i));
                    if (i < historic.size() - 1) {
                        bw.write(matrixSeparator);
                    }
                }

                bw.write(separator);

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        bw.write(String.valueOf((int) mat[i][j]));
                        //if (i < domino.getHeight() - 1 || j < domino.getWidth() - 1) {
                          //  bw.write(matrixSeparator);
                        //}
                    }
                }
                bw.write("\n");

                line = br.readLine();

            } while (line != null && !isWrited && !exist);

            if (!isWrited && !exist) {

                isWrited = true;

                idRow = domino.getIdRow();
                idCol = domino.getIdCol();

                type = domino.getType();

                height = 0;// domino.getHeight();
                width = 0;//domino.getWidth();

                historic = domino.getHistoric();

               // mat = domino.getMat();

                bw.write(type
                        + separator
                        + idRow
                        + separator
                        + idCol
                        + separator
                        + height
                        + separator
                        + width
                        + separator);
                for (int i = 0; i < historic.size(); i++) {
                    bw.write(historic.get(i));
                    if (i < historic.size() - 1) {
                        bw.write(matrixSeparator);
                    }
                }

                bw.write(separator);

                count = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        bw.write(String.valueOf((int) mat[i][j]));
                        //if (i < domino.getHeight() - 1 || j < domino.getWidth() - 1) {
                          //  bw.write(matrixSeparator);
                        //}
                    }
                }
                bw.write("\n");
            }

            while (line != null) {
                bw.write(line + "\n");
                line = br.readLine();
            }

            br.close();
            bw.close();

            br = new BufferedReader(new FileReader(fileTemp));
            bw = new BufferedWriter(new FileWriter(file));
            line = br.readLine();

            while (line != null) {
                bw.write(line + "\n");
                line = br.readLine();
            }

            br.close();
            bw.close();

            fileTemp.delete();
            return true;

        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        } finally {
            fileTemp.delete();
        }
    }
}
