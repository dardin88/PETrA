package it.unisa.petra.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 *
 * @author Antonio Prota
 */
public class StatsUI extends javax.swing.JFrame {

    /**
     * Creates new form SecondStepUI
     *
     * @param inputLocationPath
     */
    public StatsUI(String inputLocationPath) {
        initComponents();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        try {
            this.csvFile = this.mergeRunResults(inputLocationPath);
            this.updateTable("");
            this.updateBoxplot("");
        } catch (IOException ex) {
            Logger.getLogger(StatsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String mergeRunResults(String inputLocationPath) throws IOException {
        Collection<File> runResults = FileUtils.listFiles(new File(inputLocationPath), new NameFileFilter("result.csv"), TrueFileFilter.INSTANCE);
        File outputFile = new File(inputLocationPath + File.separator + "allRuns.csv");
        FileUtils.deleteQuietly(outputFile);
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            int run = 0;
            for (File runResult : runResults) {
                boolean isHeader = true;
                LineIterator it = FileUtils.lineIterator(runResult, "UTF-8");
                while (it.hasNext()) {
                    String line = it.nextLine();
                    if (isHeader == false | run == 0) {
                        pw.write(line + "\n");
                    }
                    isHeader = false;
                }
                run++;
            }
        }
        return outputFile.getAbsolutePath();
    }

    private List<ConsumptionData> filterData(String nameFilter) throws IOException {
        Reader in = new FileReader(this.csvFile);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        List<ConsumptionData> consumptionDataList = new ArrayList<>();

        Pattern pattern = Pattern.compile("^\\S*");

        for (CSVRecord record : records) {
            String signature = record.get("signature").replaceAll("^(\\.*)", "");
            double joule = Double.parseDouble(record.get(" joule"));
            double seconds = Double.parseDouble(record.get(" seconds"));

            if (signature.contains(nameFilter) || nameFilter.equals("")) {
                Matcher matcher = pattern.matcher(signature);
                matcher.find();
                ConsumptionData consumptionData = new ConsumptionData(matcher.group(), joule, seconds);
                consumptionDataList.add(consumptionData);
            }
        }

        return consumptionDataList;
    }

    private List<ConsumptionData> calculateAverages(List<ConsumptionData> filteredData) throws IOException {

        List<ConsumptionData> averageConsumptionDataList = new ArrayList<>();

        for (ConsumptionData consumptionData : filteredData) {
            String signature = consumptionData.getSignature();
            double joule = consumptionData.getJoule();
            double seconds = consumptionData.getSeconds();

            boolean found = false;
            for (ConsumptionData averageConsumptionData : averageConsumptionDataList) {
                if (signature.equals(averageConsumptionData.getSignature())) {
                    averageConsumptionData.setJoule(averageConsumptionData.getJoule() + joule);
                    averageConsumptionData.setSeconds(averageConsumptionData.getSeconds() + seconds);
                    averageConsumptionData.setNumOfTraces(averageConsumptionData.getNumOfTraces() + 1);
                    found = true;
                }
            }
            if (found == false) {
                ConsumptionData averageConsumptionData = new ConsumptionData(signature, joule, seconds);
                averageConsumptionDataList.add(averageConsumptionData);
            }
        }

        for (ConsumptionData averageConsumptionData : averageConsumptionDataList) {
            double joule = averageConsumptionData.getJoule();
            double seconds = averageConsumptionData.getSeconds();
            int numOfTraces = averageConsumptionData.getNumOfTraces();

            averageConsumptionData.setJoule(joule / numOfTraces);
            averageConsumptionData.setSeconds(seconds / numOfTraces);
        }

        return averageConsumptionDataList;
    }

    private void updateTable(String nameFilter) throws IOException {
        List<ConsumptionData> filteredDataList = this.filterData(nameFilter);
        List<ConsumptionData> averagedDataList = this.calculateAverages(filteredDataList);

        DefaultTableModel model = (DefaultTableModel) tableConsumption.getModel();
        model.setRowCount(0);
        tableConsumption.setAutoCreateRowSorter(true);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        tableConsumption.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        DecimalFormat df = new DecimalFormat("0.0000000");

        for (ConsumptionData averagedConsumptionData : averagedDataList) {
            model.addRow(new Object[]{averagedConsumptionData.getSignature(), df.format(averagedConsumptionData.getJoule()), df.format(averagedConsumptionData.getSeconds())});
        }
    }

    private void updateBoxplot(String nameFilter) throws IOException {
        List<ConsumptionData> filteredDataList = this.filterData(nameFilter);
        List<ConsumptionData> averagedDataList = this.calculateAverages(filteredDataList);

        Collections.sort(averagedDataList, (ConsumptionData o1, ConsumptionData o2) -> -Double.compare(o1.getJoule(), o2.getJoule()));

        int numOfBoxplot = 5;

        if (averagedDataList.size() < 5) {
            numOfBoxplot = averagedDataList.size();
        }

        List<ConsumptionData> mostGreedyAveragedData = averagedDataList.subList(0, numOfBoxplot);

        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        int i = 0;
        for (ConsumptionData mInstance : mostGreedyAveragedData) {
            BoxplotData mostGreedyData = new BoxplotData(mInstance.getSignature());
            for (ConsumptionData fInstance : filteredDataList) {
                if (fInstance.getSignature().equals(mInstance.getSignature())) {
                    mostGreedyData.addValue(fInstance.getJoule());
                }
            }
            dataset.add(mostGreedyData.getValues(), (i + 1) + ": " + mostGreedyData.getSignature(), (i + 1));
            i++;
        }

        String boxplot_title = "Top 5 Energy Greedy Methods";

        CategoryAxis xAxis = new CategoryAxis("Signatures");
        NumberAxis yAxis = new NumberAxis("Consumptions");

        yAxis.setAutoRangeIncludesZero(false);
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();

        renderer.setFillBox(true);
        renderer.setBaseToolTipGenerator(new BoxAndWhiskerToolTipGenerator());

        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        JFreeChart chart = new JFreeChart(
                boxplot_title,
                new Font("SansSerif", Font.BOLD, 14),
                plot,
                true
        );

        chart.setAntiAlias(true);

        ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
        chartPanel.setMouseWheelEnabled(true);

        tab2.getViewport().removeAll();
        tab2.getViewport().add(chartPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterResultsButton = new javax.swing.JButton();
        filterField = new javax.swing.JTextField();
        tabbed_panel = new javax.swing.JTabbedPane();
        tab1 = new javax.swing.JScrollPane();
        tableConsumption = new javax.swing.JTable();
        tab2 = new javax.swing.JScrollPane();
        goBack = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PETrA");
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/petra.png")).getImage());

        filterResultsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/filter.png"))); // NOI18N
        filterResultsButton.setText("Filter Results");
        filterResultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterResultsButtonActionPerformed(evt);
            }
        });

        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFieldKeyPressed(evt);
            }
        });

        tableConsumption.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Signature", "Joule", "Seconds"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableConsumption.setColumnSelectionAllowed(true);
        tab1.setViewportView(tableConsumption);
        tableConsumption.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tableConsumption.getColumnModel().getColumnCount() > 0) {
            tableConsumption.getColumnModel().getColumn(0).setPreferredWidth(500);
        }

        tabbed_panel.addTab("Average Consumptions", tab1);
        tabbed_panel.addTab("Consumption Distributions", tab2);

        goBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/back.png"))); // NOI18N
        goBack.setText("Go back");
        goBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tabbed_panel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(goBack, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filterField, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(filterResultsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterResultsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(tabbed_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(goBack)))
                .addContainerGap())
        );

        tabbed_panel.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void filterResultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterResultsButtonActionPerformed
        try {
            updateTable(filterField.getText());
            updateBoxplot(filterField.getText());
        } catch (IOException ex) {
            Logger.getLogger(StatsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_filterResultsButtonActionPerformed

    private void goBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackActionPerformed
        this.setVisible(false);
        new MainUI().setVisible(true);
    }//GEN-LAST:event_goBackActionPerformed

    private void filterFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                updateTable(filterField.getText());
                updateBoxplot(filterField.getText());
            } catch (IOException ex) {
                Logger.getLogger(StatsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_filterFieldKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterField;
    private javax.swing.JButton filterResultsButton;
    private javax.swing.JButton goBack;
    private javax.swing.JScrollPane tab1;
    private javax.swing.JScrollPane tab2;
    private javax.swing.JTabbedPane tabbed_panel;
    private javax.swing.JTable tableConsumption;
    // End of variables declaration//GEN-END:variables
    private String csvFile;
}
