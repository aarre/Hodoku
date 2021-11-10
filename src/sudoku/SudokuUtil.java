/*
 * Copyright (C) 2008-12  Bernhard Hobiger
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */
package sudoku;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.FontUIResource;

/**
 * @author hobiwan
 */
public class SudokuUtil {
    /**
     * The correct line separator for the current platform
     */
    public static String NEW_LINE = System.getProperty("line.separator");

    /**
     * A global PrinterJob; is here and not in {@link Options} because it needs a getter but should not be written to a
     * configuration file.
     */
    private static PrinterJob printerJob;
    /**
     * A global PageFormat; is here and not in {@link Options} because it needs a getter but should not be written to a
     * configuration file.
     */
    private static PageFormat pageFormat;

    /**
     * The name of the look and feel to apply.
     */
    private static String lookAndFeelClassName;

    /**
     * HiRes printing in Java is difficult: The whole printing engine (except apparently text printing) is scaled down
     * to 72dpi. This is done by applying an AffineTransform object with a scale to the Graphics2D object of the
     * printer. To make things more complicated just reversing the scale is not enough: for Landscape printing a
     * rotation is applied after the scale.
     * <p>
     * The easiest way to really achieve hires printing is to directly manipulate the transformation matrix. The default
     * matrix looks like this:
     * <pre>
     *      Portrait     Landscape
     *    [ d  0  x ]   [  0  d  x ]
     *    [ 0  d  y ]   [ -d  0  y ]
     *    [ 0  0  1 ]   [  0  0  1 ]
     *
     *    d = printerResolution / 72.0
     * </pre>
     * x and y are set by the printer engine and should not be changed.
     * <p>
     * The values from the {@link PageFormat} object are scaled down to 72dpi as well and have to be multiplied with d
     * to get the correct hires values.
     *
     * @param g2
     * @return The scale factor
     */
    public static double adjustGraphicsForPrinting(Graphics2D g2) {
        AffineTransform at = g2.getTransform();
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        // System.out.println("matrix: " + Arrays.toString(matrix));
        double scale = matrix[0];
        if (scale != 0) {
            // Portrait
            matrix[0] = 1;
            matrix[3] = 1;
        } else {
            // Landscape
            scale = matrix[2];
            matrix[1] = -1;
            matrix[2] = 1;
        }
//        int resolution = (int)(72.0 * scale);
        AffineTransform newAt = new AffineTransform(matrix);
        g2.setTransform(newAt);
        return scale;
    }

    /**
     * Change the font size for the Nimbus look and feel.
     * <p>
     * Due to late initialization issues with the Nimbus look and feel, the standard method for changing fonts leads to
     * unpredictable results (see
     * <a href="http://stackoverflow.com/questions/949353/java-altering-ui-fonts-nimbus-doesnt-work">this
     * StackOverflow post</a> for details).
     * <p>
     * Changing the font size in Nimbus can be done in one of two ways:
     *
     * <ul>
     * <li>Subclass <code>NimbusLookAndFeel</code> and override <code>getDefaults()</code></li>
     * <li>Obtain an instance of <code>NimbusLookAndFeel</code> and set the <code>defaultFont</code> option on the
     * instance directly (<b>not</b> on <code>UIManager.getDefaults()</code>).</li>
     * </ul>
     * <p>
     * In addition, the package of the <code>NimbusLookAndFeel</code> class changed between Java 1.6
     * (<code>sun.swing.plaf.nimbus</code>) and 1.7 (<code>javax.swing.plaf.nimbus</code>). That means that if the
     * class is subclassed or instantiated directly, code compiled with 1.7 will not start on 1.6 and vice versa (and of
     * course the program will not start on all JRE versions, that don't have Nimbus included). And we have to think of
     * the possibility that the class stored in {@link Options#laf} doesn't exist at all, if the hcfg file is moved
     * between platforms. Therefore, we use the strategy of obtaining an instance of <code>NimbusLookAndFeel</code> and
     * setting the <code>defaultFont</code> option on the instance directly.
     */
    private static void changeFontSizeForNimbus(final LookAndFeel lookAndFeel, final int customFontSize) {
        final UIDefaults uiDefaults = lookAndFeel.getDefaults();
        final Object defaultFont = uiDefaults.get("defaultFont");
        if (null != defaultFont) {
            // "defaultFont" exists on Nimbus and triggers inheritance
            final Font font = (Font) defaultFont;
            final int fontSize = font.getSize();
            if (fontSize != customFontSize) {
                Logger.getLogger(SudokuUtil.class.getName()).log(Level.CONFIG,
                        "Changing font size for Nimbus look and feel from {0} to {1}",
                        new Object[]{fontSize, customFontSize});
                final String fontName = font.getName();
                final int fontStyle = font.getStyle();
                uiDefaults.put("defaultFont", new FontUIResource(fontName, fontStyle, customFontSize));
            }
        }
    }

    /**
     * Change the font size for non-Nimbus look and feels.
     * <p>
     * On most LaFs (GTK excluded because nothing can be changed in GTK LaF), changing the font size works by changing
     * all Font instances in <code>UIManager.getDefaults()</code>.
     *
     * @param customFontSize The desired font size
     */
    private static void changeFontSizeForNonNimbus(final int customFontSize) {
        final Logger logger = Logger.getLogger(SudokuUtil.class.getName());
        logger.setLevel(Level.FINEST);
        final UIDefaults uiDefaults = UIManager.getDefaults();
        final Enumeration<Object> uiDefaultsKeys = uiDefaults.keys();
        while (uiDefaultsKeys.hasMoreElements()) {
            final Object uiDefaultKey = uiDefaultsKeys.nextElement();
            if (Objects.equals(uiDefaultKey.toString(), "defaultFont")) {
                logger.log(Level.FINEST, "Found defaultFont key");
            }
            final Object uiManagerValue = UIManager.get(uiDefaultKey);
            if (uiManagerValue instanceof FontUIResource) {
                final Font font = UIManager.getFont(uiDefaultKey);
                if (null != font) {
                    final int fontSize = font.getSize();
                    if (fontSize != customFontSize) {
                        final String fontName = font.getName();
                        final int fontStyle = font.getStyle();
                        logger.log(Level.CONFIG,
                                "Changing size of font {0} ({1} {2}) for non-Nimbus look and feel from {3} to {4}",
                                new Object[]{uiDefaultKey, fontName, fontStyle, fontSize, customFontSize});
                        final FontUIResource newFontUIResource = new FontUIResource(fontName, fontStyle, customFontSize);
                        UIManager.put(uiDefaultKey, newFontUIResource);
                    }
                }
            }
        }
        logger.setLevel(Level.CONFIG);
    }

    /**
     * Clears the list. The steps are not nullfied, but the list items are.
     *
     * @param steps
     */
    public static void clearStepList(List<SolutionStep> steps) {
        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                steps.set(i, null);
            }
            steps.clear();
        }
    }

    /**
     * Clears the list. To avoid memory leaks all steps in the list are explicitly nullified.
     *
     * @param steps
     */
    public static void clearStepListWithNullify(List<SolutionStep> steps) {
        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).reset();
                steps.set(i, null);
            }
            steps.clear();
        }
    }

    /**
     * Calculates n over k
     *
     * @param n
     * @param k
     * @return
     */
    public static int combinations(int n, int k) {
        if (n <= 167) {
            double fakN = 1;
            for (int i = 2; i <= n; i++) {
                fakN *= i;
            }
            double fakNMinusK = 1;
            for (int i = 2; i <= n - k; i++) {
                fakNMinusK *= i;
            }
            double fakK = 1;
            for (int i = 2; i <= k; i++) {
                fakK *= i;
            }
            return (int) (fakN / (fakNMinusK * fakK));
        } else {
            BigInteger fakN = BigInteger.ONE;
            for (int i = 2; i <= n; i++) {
                fakN = fakN.multiply(new BigInteger(i + ""));
            }
            BigInteger fakNMinusK = BigInteger.ONE;
            for (int i = 2; i <= n - k; i++) {
                fakNMinusK = fakNMinusK.multiply(new BigInteger(i + ""));
            }
            BigInteger fakK = BigInteger.ONE;
            for (int i = 2; i <= k; i++) {
                fakK = fakK.multiply(new BigInteger(i + ""));
            }
            fakNMinusK = fakNMinusK.multiply(fakK);
            fakN = fakN.divide(fakNMinusK);
            return fakN.intValue();
        }
    }

    /**
     * STUB!!
     * <p>
     * Is meant for replacing candidate numbers with colors for colorKu. Doesnt do anything meaningful right now.
     *
     * @param candidate
     * @return
     */
    public static String getCandString(int candidate) {
        if (Options.getInstance().isShowColorKuAct()) {
            // return some color name here
            return String.valueOf(candidate);
        } else {
            return String.valueOf(candidate);
        }
    }

    /**
     * Get the name associated with the look and feel class.
     * <p>
     * This is necessary because {@link UIManager#createLookAndFeel(String)} takes a look and feel name, not a look and
     * feel class name.
     *
     * @return The name of the look and feel class stored in {@link #lookAndFeelClassName}
     */
    private static String getLookAndFeelName() {
        String result = null;
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            Logger.getLogger(Main.class.getName()).log(Level.FINEST, "Checking class name {0} / {1}",
                    new Object[]{info.getName(), info.getClassName()});
            if (info.getClassName().equals(lookAndFeelClassName)) {
                result = info.getName();
                break;
            }
        }
        return result;
    }

    /**
     * Get a Nimbus LookAndFeel instance.
     * <p>
     * Do not set the Nimbus look and feel explicitly by invoking the UIManager.setLookAndFeel method because not all
     * versions or implementations of Java SE 6 support Nimbus. Additionally, the location of the Nimbus package changed
     * between the 6u10 and JDK7 releases. Iterating through all installed look and feel implementations is a more
     * robust approach because, if Nimbus is not available, the default look and feel can be used. For the Java SE 6
     * Update 10 release, the Nimbus package is located at com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel.
     *
     * @return A Nimbus LookAndFeel instance or null if Nimbus is not available
     */
    private static LookAndFeel getNimbusLookAndFeel() {
        LookAndFeel result = null;
        final String requestedLaFClassName = lookAndFeelClassName;
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                Logger.getLogger(Main.class.getName()).log(Level.FINEST, "Checking look and feel {0} / {1}",
                        new Object[]{info.getName(), info.getClassName()});
                if ("Nimbus".equals(info.getName())) {
                    result = UIManager.createLookAndFeel("Nimbus");
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            // Nimbus is not supported, so set the GUI to another look and feel.
            lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            Logger.getLogger(Main.class.getName()).log(Level.CONFIG, "ERROR 1002: Look and feel {0} not supported, " +
                    "switching to {1}", new Object[]{requestedLaFClassName, lookAndFeelClassName});
        }
        // Nimbus is not in the list of installed look and feels, so set the GUI to another look and feel.
        if (result == null) {
            lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            Logger.getLogger(Main.class.getName()).log(Level.CONFIG, "ERROR 1003: Look and feel {0} not installed, " +
                    "switching to {1}", new Object[]{requestedLaFClassName, lookAndFeelClassName});
        }
        return result;
    }

    /**
     * Get a non-Nimbus LookAndFeel instance.
     *
     * @return A LookAndFeel instance for a non-Nimbus look and feel
     */
    private static LookAndFeel getNonNimbusLookAndFeel() {
        LookAndFeel result = null;
        String lafName = null;
        try {
            lafName = getLookAndFeelName();
            result = UIManager.createLookAndFeel(lafName);
            
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "ERROR 1001: Unable to change look and feel to " + lafName + " / " + lookAndFeelClassName, ex);
        }
        return result;
    }

    /**
     * @return the pageFormat
     */
    public static PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = getPrinterJob().defaultPage();
        }
        return pageFormat;
    }

    /**
     * @return the printerJob
     */
    public static PrinterJob getPrinterJob() {
        if (printerJob == null) {
            printerJob = PrinterJob.getPrinterJob();
        }
        return printerJob;
    }

    /**
     * Reformat a sudoku given by a 81 character string to SimpleSudoku format.
     *
     * @param values
     * @return
     */
    public static String getSSFormatted(String values) {
        StringBuilder tmp = new StringBuilder();
        values = values.replace('0', '.');
        tmp.append(" *-----------*");
        tmp.append(NEW_LINE);
        writeSSLine(tmp, values, 0);
        writeSSLine(tmp, values, 9);
        writeSSLine(tmp, values, 18);
        tmp.append(" |---+---+---|");
        tmp.append(NEW_LINE);
        writeSSLine(tmp, values, 27);
        writeSSLine(tmp, values, 36);
        writeSSLine(tmp, values, 45);
        tmp.append(" |---+---+---|");
        tmp.append(NEW_LINE);
        writeSSLine(tmp, values, 54);
        writeSSLine(tmp, values, 63);
        writeSSLine(tmp, values, 72);
        tmp.append(" *-----------*");
        tmp.append(NEW_LINE);
        return tmp.toString();
    }

    /**
     * Reformat a HoDoKu PM grid to SimpleSudoku format.
     *
     * @param grid
     * @return
     */
    public static String getSSPMGrid(String grid) {
//        .---------------.------------.-------------.
//        | 1   78    38  | 2   49  6  | 47  39  5   |
//        | 9   67    5   | 3   8   14 | 47  16  2   |
//        | 36  4     2   | 19  7   5  | 8   36  19  |
//        :---------------+------------+-------------:
//        | 8   9     7   | 5   6   2  | 13  4   13  |
//        | 25  25    1   | 4   3   8  | 9   7   6   |
//        | 4   3     6   | 7   1   9  | 5   2   8   |
//        :---------------+------------+-------------:
//        | 36  16    4   | 8   5   7  | 2   19  139 |
//        | 7   158   89  | 19  2   3  | 6   58  4   |
//        | 25  1258  389 | 6   49  14 | 3   58  7   |
//        '---------------'------------'-------------'
        // parse the grid
        String[] parts = grid.split(" ");
        String[] cells = new String[81];
        int maxLength = 0;
        for (int i = 0, j = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            char ch = parts[i].charAt(0);
            if (Character.isDigit(ch)) {
                cells[j++] = parts[i];
                if (parts[i].length() > maxLength) {
                    maxLength = parts[i].length();
                }
            }
        }
        // now make all cells equally long
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].length() < maxLength) {
                int anz = maxLength - cells[i].length();
                for (int j = 0; j < anz; j++) {
                    cells[i] += " ";
                }
            }
        }
        // build the grid
        StringBuilder tmp = new StringBuilder();
        writeSSPMFrameLine(tmp, maxLength, true);
        writeSSPMLine(tmp, cells, 0);
        writeSSPMLine(tmp, cells, 9);
        writeSSPMLine(tmp, cells, 18);
        writeSSPMFrameLine(tmp, maxLength, false);
        writeSSPMLine(tmp, cells, 27);
        writeSSPMLine(tmp, cells, 36);
        writeSSPMLine(tmp, cells, 45);
        writeSSPMFrameLine(tmp, maxLength, false);
        writeSSPMLine(tmp, cells, 54);
        writeSSPMLine(tmp, cells, 63);
        writeSSPMLine(tmp, cells, 72);
        writeSSPMFrameLine(tmp, maxLength, true);

        return tmp.toString();
    }

    /**
     * Prints the default font settings to stdout. Used for debugging only.
     */
    public static void printFontDefaults() {
        System.out.println("Default font settings: UIManager");
        UIDefaults def = UIManager.getDefaults();
        SortedMap<String, String> items = new TreeMap<String, String>();
        // def.keySet() doesnt seem to work -> use def.keys() instead!
        Enumeration<Object> keys = def.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Font font = def.getFont(key);
            if (font != null) {
                items.put(key.toString(), font.getName() + "/" + font.getStyle() + "/" + font.getSize());
            }
        }
        Set<Entry<String, String>> entries = items.entrySet();
        for (Entry<String, String> act : entries) {
            System.out.println("     " + act.getKey() + ": " + act.getValue());
        }
    }

    /**
     * Set the look and feel to the class whose name is stored in {@link Options#laf}.
     * <p>
     * To make HoDoKu behave nicely for visually impaired users, a nonstandard font size {@link Options#customFontSize}
     * can be used for all GUI elements, if {@link Options#useDefaultFontSize} is set to <code>false</code>.
     */
    public static void setLookAndFeel() {
        // ok: start by getting the correct AND existing LaF class
        LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        boolean found = false;
        lookAndFeelClassName = Options.getInstance().getLaf();
        String storedLaFClassName = lookAndFeelClassName;
        if (!lookAndFeelClassName.isEmpty()) {
            String lafName = lookAndFeelClassName.substring(lookAndFeelClassName.lastIndexOf('.') + 1);
            for (int i = 0; i < lafs.length; i++) {
                if (lafs[i].getClassName().equals(lookAndFeelClassName)) {
                    found = true;
                    break;
                } else if (lafs[i].getClassName().endsWith(lafName)) {
                    // same class, different package
                    lookAndFeelClassName = lafs[i].getClassName();
                    Logger.getLogger(Main.class.getName()).log(Level.CONFIG, "laf package changed from {0} to {1}",
                            new Object[]{storedLaFClassName, lookAndFeelClassName});
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            // class not present or default requested
            Options.getInstance().setLaf("");
            lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
        } else {
            if (!storedLaFClassName.equals(lookAndFeelClassName)) {
                Options.getInstance().setLaf(lookAndFeelClassName);
            }
        }

        // ok, the correct class name is now in lookAndFeelClassName
        // -> obtain an lookAndFeel of the LaF class
        LookAndFeel lookAndFeel = null;

        // Try to get a Nimbus look and feel if necessary
        if (lookAndFeelClassName.contains("Nimbus")) {
            lookAndFeel = getNimbusLookAndFeel();
        }

        // If the Nimbus look and feel has not been set, get another look and feel
        if (lookAndFeel == null) {
            lookAndFeel = getNonNimbusLookAndFeel();
        }

        // we have a look and feel: try setting it
        try {

            int customFontSize = Options.getInstance().getCustomFontSize();

            // Change the font size for the Nimbus look and feel before setting the look and feel
            if ((lookAndFeelClassName.contains("Nimbus")) && (!Options.getInstance().isUseDefaultFontSize())) {
                changeFontSizeForNimbus(lookAndFeel, customFontSize);
            }

            // set the new LaF
            UIManager.setLookAndFeel(lookAndFeel);
            Logger.getLogger(SudokuUtil.class.getName()).log(Level.CONFIG, "Look and feel set to {0}", UIManager.getLookAndFeel().getName());

            // Change the font size for non-Nimbus look and feels after setting the look and feel
            if ((!lookAndFeelClassName.contains("Nimbus")) && (!Options.getInstance().isUseDefaultFontSize())) {
                changeFontSizeForNonNimbus(customFontSize);
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "ERROR 1000: Unable to change look and feel to " + lookAndFeelClassName, ex);
            Logger.getLogger(Main.class.getName()).log(Level.FINEST, "Available look and feels:");
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                Logger.getLogger(Main.class.getName()).log(Level.FINEST, "Look and feel {0} / {1}",
                        new Object[]{info.getClassName(), info.getName()});
            }
        }
    }

    /**
     * Build one line of a sudoku defined by <code>clues</code> in SimpleSudoku format.
     *
     * @param tmp
     * @param clues
     * @param startIndex
     */
    private static void writeSSLine(StringBuilder tmp, String clues, int startIndex) {
        tmp.append(" |");
        tmp.append(clues.substring(startIndex + 0, startIndex + 3));
        tmp.append("|");
        tmp.append(clues.substring(startIndex + 3, startIndex + 6));
        tmp.append("|");
        tmp.append(clues.substring(startIndex + 6, startIndex + 9));
        tmp.append("|");
        tmp.append(NEW_LINE);
    }

    /**
     * Write one frame line for a SimpleSudoku PM grid.
     *
     * @param tmp
     * @param maxLength
     * @param outer
     */
    private static void writeSSPMFrameLine(StringBuilder tmp, int maxLength, boolean outer) {
        tmp.append(" *");
        for (int i = 0; i < 3 * maxLength + 7; i++) {
            tmp.append("-");
        }
        if (outer) {
            tmp.append("-");
        } else {
            tmp.append("+");
        }
        for (int i = 0; i < 3 * maxLength + 7; i++) {
            tmp.append("-");
        }
        if (outer) {
            tmp.append("-");
        } else {
            tmp.append("+");
        }
        for (int i = 0; i < 3 * maxLength + 7; i++) {
            tmp.append("-");
        }
        if (outer) {
            tmp.append("*");
        } else {
            tmp.append("|");
        }
        tmp.append(NEW_LINE);
    }

    /**
     * Write one line containing cells for a SimpleSudoku PM grid.
     *
     * @param tmp
     * @param cells
     * @param index
     */
    private static void writeSSPMLine(StringBuilder tmp, String[] cells, int index) {
        tmp.append(" | ");
        tmp.append(cells[index + 0]);
        tmp.append("  ");
        tmp.append(cells[index + 1]);
        tmp.append("  ");
        tmp.append(cells[index + 2]);
        tmp.append("  | ");
        tmp.append(cells[index + 3]);
        tmp.append("  ");
        tmp.append(cells[index + 4]);
        tmp.append("  ");
        tmp.append(cells[index + 5]);
        tmp.append("  | ");
        tmp.append(cells[index + 6]);
        tmp.append("  ");
        tmp.append(cells[index + 7]);
        tmp.append("  ");
        tmp.append(cells[index + 8]);
        tmp.append("  |");
        tmp.append(NEW_LINE);
    }

    /**
     * testing...
     *
     * @param args
     */
	/*
	public static void main(String[] args) {
		String grid = ".---------------.------------.-------------." + "| 1   78    38  | 2   49  6  | 47  39  5   |"
				+ "| 9   67    5   | 3   8   14 | 47  16  2   |" + "| 36  4     2   | 19  7   5  | 8   36  19  |"
				+ ":---------------+------------+-------------:" + "| 8   9     7   | 5   6   2  | 13  4   13  |"
				+ "| 25  25    1   | 4   3   8  | 9   7   6   |" + "| 4   3     6   | 7   1   9  | 5   2   8   |"
				+ ":---------------+------------+-------------:" + "| 36  16    4   | 8   5   7  | 2   19  139 |"
				+ "| 7   158   89  | 19  2   3  | 6   58  4   |" + "| 25  1258  389 | 6   49  14 | 3   58  7   |"
				+ "'---------------'------------'-------------'";
		getSSPMGrid(grid);
	}*/
}
