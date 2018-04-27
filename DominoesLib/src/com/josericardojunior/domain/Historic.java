package com.josericardojunior.domain;

public class Historic {

    private static final String separatorLeft = "<";
    private static final String separatorRight = ">";
    private static final String itemSeparator = ",";

    private String rightItem;
    private String leftItem;
    private Dominoes dominoLeft;
    private Dominoes dominoRight;
    private Historic historicLeft;
    private Historic historicRight;
    private int length;

    public Historic(String treeHistoric) {
        if (!isTreeFormat(treeHistoric)) {
            try {
                this.finalize();
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Invalid argument.\n"
                        + "The treeFormat parameter not is a tree format");
            }
        }

        String[] split = treeHistoric.split(Historic.itemSeparator);
        int indexSeparator = (split.length) / 2;
        String aux = "";
        for (int i = 0; i < indexSeparator; i++) {
            aux = aux.concat(split[i]);
            if (i < indexSeparator - 1) {
                aux = aux.concat(Historic.itemSeparator);
            }
        }
        String leftTree = aux.substring(1);
        String rightTree = treeHistoric.substring(aux.length() + 1, treeHistoric.length() - 1);

        Historic temp = new Historic(leftTree, rightTree);
        this.setHistoric(temp.leftItem, temp.rightItem);
        this.historicLeft = temp.historicLeft;
        this.historicRight = temp.historicRight;
        this.length = temp.length;
        this.dominoLeft = temp.dominoLeft;
        this.dominoRight = temp.dominoRight;
    }

    public Historic(String left, String right) throws IllegalArgumentException {
        this.setHistoric(left, right);

        if (!this.isTreeFormat(left)) {
            this.historicLeft = null;
            this.length = 1;
        } else {
            this.historicLeft = new Historic(left);
            this.length = this.historicLeft.length;
        }
        if (!this.isTreeFormat(right)) {
            this.historicRight = null;
            this.length += 1;
        } else {
            this.historicRight = new Historic(right);
            this.length += this.historicRight.length;
        }

    }

    public Historic(Historic left, Historic right) {
        
        this.historicLeft = left;
        this.historicRight = right;

        this.leftItem = left.toString();
        this.rightItem = right.toString();

        this.length = left.length + right.length;
    }

    private Historic() {

    }

    public String getTreeFormat() {
        if (this.historicLeft == null || this.historicRight == null) {
            return Historic.separatorLeft
                    + this.leftItem
                    + Historic.itemSeparator
                    + this.rightItem
                    + Historic.separatorRight;
        }
        return Historic.separatorLeft
                + this.historicLeft.getTreeFormat()
                + Historic.itemSeparator
                + this.historicRight.getTreeFormat()
                + Historic.separatorRight;
    }

    public String toString() {
        String result = "";
        if (this.historicLeft == null) {
            result = this.leftItem;
        } else {
            result = this.historicLeft.toString();
        }

        if (this.historicRight == null) {
            result += this.rightItem;
        } else {
            result += this.historicRight.toString();
        }
        return result;
    }

    /**
     * Used to change the Historic
     *
     * @param historic The Historic value
     * @throws IllegalArgumentException
     */
    private void setHistoric(String left, String right) throws IllegalArgumentException {
        if (left == null || left.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe Historic"
                    + " on the Left attribute is null, void or invalid");
        }

        if (right == null || right.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe Historic"
                    + " on the Right attribute is null, void or invalid");
        }

        this.leftItem = left;
        this.rightItem = right;

    }

    /**
     * This function is used to add to right, a new historic
     *
     * @param historic Use example: [A,B] + [C,D] => [[A,B],[C,D]]
     * @throws IllegalArgumentException
     */
    public void addHistoric(Historic historic) throws IllegalArgumentException {
        if (historic == null
                || historic.toString().toString() == null
                || historic.toString().toString().trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe Historic"
                    + " attribute is null or void");
        }
//        if (!this.getItem(this.length - 1).equals(historic.getItem(0))) {
//            throw new IllegalArgumentException("Invalid argument.\nThe Historic"
//                    + " to be added is invalid. the last item of this Historic"
//                    + " is not equal the first item of the Historic in the"
//                    + " parameter");
//        }
        if (this == historic) {
            historic = new Historic();

            historic.historicLeft = this.historicLeft;
            historic.historicRight = this.historicRight;
            historic.leftItem = this.leftItem;
            historic.rightItem = this.rightItem;
            historic.length = this.length;
        }
        Historic newHist = new Historic();

        newHist.historicLeft = this.historicLeft;
        newHist.historicRight = this.historicRight;
        newHist.leftItem = this.leftItem;
        newHist.rightItem = this.rightItem;
        newHist.length = this.length;

        this.historicLeft = newHist;
        this.historicRight = historic;
        this.leftItem = newHist.toString();
        this.rightItem = historic.toString();
        this.length = newHist.length + historic.length;

    }

    /**
     * This function will return the item corresponding to index 
     * 
     * <p><b>Exemple:</b>
     * <blockquote>suppose the historic [[A,B],[C,D]]. To the index: 
     * <br>1 => A
     * <br>2 => B
     * <br>3 => C
     * <br>4 => D
     * </blockquote>
     * 
     * @param index associated to item
     * @throws IllegalArgumentException in cases where the index is invalid
     *
     */
    public String getItem(int index) throws IllegalArgumentException {
        Historic result = this;

        while (result.length > 2) {

            if (index < result.historicLeft.length) {
                result = result.historicLeft;
                continue;
            } else {
                index -= result.historicLeft.length;
                result = result.historicRight;
                continue;
            }
        }
        if (index == 0) {
            return result.leftItem;
        } else if (index == 1) {
            return result.rightItem;
        }
        throw new IllegalArgumentException("Invalid argument.\nThe Index"
                + " parameter is out of bound");
    }

    public String getRightItem() {
        return rightItem;
    }

    public String getLeftItem() {
        return leftItem;
    }

    public String getFirstItem() {
        return this.getItem(0);
    }

    public void reduceRow() {
    	Historic left = this;
    	while(left != null){
    		left.leftItem = Dominoes.AGGREG_TEXT + left.leftItem;
    		left = left.historicLeft;
    	}
    }
    
    public String getLastItem() {
        return this.getItem(length - 1);
    }

    public Historic getHistoricLeft() {
        return historicLeft;
    }

    public Historic getHistoricRight() {
        return historicRight;
    }

    public int getLength() {
        return length;
    }

    public void reverse() {
        Historic aux = this;
        Historic swapHist = null;

        String swapItem = null;

        swapHist = aux.historicLeft;
        aux.historicLeft = aux.historicRight;
        aux.historicRight = swapHist;

        swapItem = new StringBuffer(aux.leftItem).reverse().toString();
        aux.leftItem = new StringBuffer(aux.rightItem).reverse().toString();
        aux.rightItem = swapItem;
     
        if (aux.historicLeft != null) {
            aux.historicLeft.reverse();
        }else{
            aux.leftItem = new StringBuffer(aux.leftItem).reverse().toString();
        }
        
        if (aux.historicRight != null) {
            aux.historicRight.reverse();
        }else{
            aux.rightItem = new StringBuffer(aux.rightItem).reverse().toString();
        }

    }

    private boolean isTreeFormat(String format) {
        if (format.split(Historic.itemSeparator).length == format.split(Historic.separatorLeft).length
                && format.split(Historic.itemSeparator).length == format.split(Historic.separatorRight).length + 1) {

            boolean result = true;

            String[] split = format.split(Historic.itemSeparator);
            int indexSeparator = (split.length) / 2;
            String aux = "";
            for (int i = 0; i < indexSeparator; i++) {
                aux = aux.concat(split[i]);
                if (i < indexSeparator - 1) {
                    aux = aux.concat(Historic.itemSeparator);
                }
            }

            String leftTree = aux.substring(1);
            String rightTree = format.substring(aux.length() + 1, format.length() - 1);

            if (leftTree.startsWith(Historic.separatorLeft)) {
                result = result && this.isTreeFormat(leftTree);
            }
            if (rightTree.endsWith(Historic.separatorRight)) {
                result = result && this.isTreeFormat(rightTree);
            }

            return result;
        }
        return false;
    }
}
