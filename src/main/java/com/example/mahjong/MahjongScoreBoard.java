package com.example.mahjong;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MahjongScoreBoard extends JFrame {
    private static final int PLAYER_COUNT = 4;
    private static final int GAME_COUNT = 7;
    private static final Color GAME_ROW_COLOR = new Color(204, 236, 246);
    private static final Color TOTAL_ROW_COLOR = new Color(255, 192, 0);
    private static final Color MINUS_COLOR = new Color(190, 30, 45);
    private static final Font APP_FONT = new Font("BIZ UDPゴシック", Font.PLAIN, 14);
    private static final DecimalFormat POINT_FORMAT = new DecimalFormat("#,##0");

    private final JTextField startPointField = new JTextField("25,000", 7);
    private final JTextField returnPointField = new JTextField("30,000", 7);
    private final JComboBox<String> umaComboBox = new JComboBox<>(new String[]{"10-20", "10-30", "5-10", "なし"});
    private final DefaultTableModel model;
    private final JTable table;
    private final double[][] gameScores = new double[GAME_COUNT][PLAYER_COUNT];
    private final boolean[] calculatedGames = new boolean[GAME_COUNT];

    public MahjongScoreBoard() {
        super("麻雀スコア集計");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBorder(new EmptyBorder(8, 8, 0, 8));
        topPanel.add(new JLabel("持ち点"));
        topPanel.add(startPointField);
        topPanel.add(new JLabel("返し"));
        topPanel.add(returnPointField);
        topPanel.add(new JLabel("ウマ"));
        topPanel.add(umaComboBox);

        String[] columns = {"試合", "1", "2", "3", "4"};
        model = new DefaultTableModel(columns, GAME_COUNT + 3) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row == 1 && column >= 1 && column <= PLAYER_COUNT) {
                    return true;
                }
                return row >= 3 && row < 3 + GAME_COUNT && column >= 1 && column <= PLAYER_COUNT;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(APP_FONT);
        table.getTableHeader().setFont(APP_FONT);
        table.getTableHeader().setReorderingAllowed(false);
        table.setTableHeader(null);
        table.setDefaultRenderer(Object.class, new ScoreCellRenderer());
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(128);
        }

        setupTableValues();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(0, 8, 8, 8));
        JButton calculateAllButton = new JButton("全試合を計算");
        calculateAllButton.addActionListener(event -> calculateAllGames());
        JButton resetButton = new JButton("リセット");
        resetButton.addActionListener(event -> resetAll());
        bottomPanel.add(resetButton);
        bottomPanel.add(calculateAllButton);

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        applyFont(this);
        setSize(820, 520);
        setLocation(80, 80);
        setAlwaysOnTop(true);
    }

    private void setupTableValues() {
        model.setValueAt("順位", 0, 0);
        for (int player = 1; player <= PLAYER_COUNT; player++) {
            model.setValueAt(player + "位", 0, player);
        }

        model.setValueAt("氏名", 1, 0);
        for (int player = 1; player <= PLAYER_COUNT; player++) {
            model.setValueAt("", 1, player);
        }

        model.setValueAt("TOTAL", 2, 0);
        for (int player = 1; player <= PLAYER_COUNT; player++) {
            model.setValueAt("0.0", 2, player);
        }

        for (int game = 0; game < GAME_COUNT; game++) {
            int row = game + 3;
            model.setValueAt(game + 1, row, 0);
            for (int player = 1; player <= PLAYER_COUNT; player++) {
                model.setValueAt("", row, player);
            }
        }
    }

    private void calculateGame(int gameIndex) {
        try {
            int[] points = readPoints(gameIndex);
            double[] scores = calculateScores(points);
            for (int player = 0; player < PLAYER_COUNT; player++) {
                gameScores[gameIndex][player] = scores[player];
                model.setValueAt(formatScore(scores[player]), gameIndex + 3, player + 1);
            }
            calculatedGames[gameIndex] = true;
            updateTotals();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "入力エラー", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void calculateAllGames() {
        for (int game = 0; game < GAME_COUNT; game++) {
            if (calculatedGames[game]) {
                continue;
            }
            boolean hasInput = false;
            for (int player = 1; player <= PLAYER_COUNT; player++) {
                Object value = model.getValueAt(game + 3, player);
                if (value != null && !value.toString().trim().isEmpty()) {
                    hasInput = true;
                    break;
                }
            }
            if (hasInput) {
                calculateGame(game);
            }
        }
    }

    private int[] readPoints(int gameIndex) {
        int[] points = new int[PLAYER_COUNT];
        for (int player = 0; player < PLAYER_COUNT; player++) {
            Object rawValue = model.getValueAt(gameIndex + 3, player + 1);
            if (rawValue == null || rawValue.toString().trim().isEmpty()) {
                throw new IllegalArgumentException((gameIndex + 1) + "試合目の得点を4人分入力してください。");
            }
            points[player] = parseInteger(rawValue.toString(), "得点");
            model.setValueAt(formatPoint(points[player]), gameIndex + 3, player + 1);
        }
        return points;
    }

    private double[] calculateScores(int[] points) {
        int startPoint = parseInteger(startPointField.getText(), "持ち点");
        int returnPoint = parseInteger(returnPointField.getText(), "返し点");
        Uma uma = Uma.from((String) umaComboBox.getSelectedItem());
        double oka = (returnPoint - startPoint) * PLAYER_COUNT / 1000.0;

        List<PlayerResult> results = new ArrayList<>();
        for (int i = 0; i < PLAYER_COUNT; i++) {
            results.add(new PlayerResult(i, points[i]));
        }
        results.sort(Comparator.comparingInt(PlayerResult::point).reversed());

        double[] scores = new double[PLAYER_COUNT];
        for (int rank = 0; rank < results.size(); rank++) {
            PlayerResult result = results.get(rank);
            double score = (result.point() - returnPoint) / 1000.0 + uma.valueForRank(rank + 1);
            if (rank == 0) {
                score += oka;
            }
            scores[result.index()] = score;
        }
        return scores;
    }

    private void updateTotals() {
        double[] totals = new double[PLAYER_COUNT];
        for (int game = 0; game < GAME_COUNT; game++) {
            for (int player = 0; player < PLAYER_COUNT; player++) {
                totals[player] += gameScores[game][player];
            }
        }

        List<PlayerResult> ranking = new ArrayList<>();
        for (int player = 0; player < PLAYER_COUNT; player++) {
            model.setValueAt(formatScore(totals[player]), 2, player + 1);
            ranking.add(new PlayerResult(player, (int) Math.round(totals[player] * 10)));
        }
        ranking.sort(Comparator.comparingInt(PlayerResult::point).reversed());
        for (int rank = 0; rank < ranking.size(); rank++) {
            model.setValueAt((rank + 1) + "位", 0, ranking.get(rank).index() + 1);
        }
    }

    private int parseInteger(String value, String label) {
        try {
            return Integer.parseInt(value.replace(",", "").trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + "は数値で入力してください。");
        }
    }

    private String formatScore(double score) {
        return String.format("%.1f", score);
    }

    private String formatPoint(int point) {
        return POINT_FORMAT.format(point);
    }

    private void resetAll() {
        startPointField.setText("25,000");
        returnPointField.setText("30,000");
        umaComboBox.setSelectedItem("10-20");
        for (int game = 0; game < GAME_COUNT; game++) {
            for (int player = 0; player < PLAYER_COUNT; player++) {
                gameScores[game][player] = 0;
                model.setValueAt("", game + 3, player + 1);
            }
            calculatedGames[game] = false;
        }
        for (int player = 1; player <= PLAYER_COUNT; player++) {
            model.setValueAt(player + "位", 0, player);
            model.setValueAt("", 1, player);
            model.setValueAt("0.0", 2, player);
        }
    }

    private void applyFont(Container container) {
        container.setFont(APP_FONT);
        for (Component component : container.getComponents()) {
            component.setFont(APP_FONT);
            if (component instanceof Container child) {
                applyFont(child);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MahjongScoreBoard frame = new MahjongScoreBoard();
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });
    }

    private record PlayerResult(int index, int point) {
    }

    private record Uma(double second, double first) {
        static Uma from(String value) {
            if ("10-30".equals(value)) {
                return new Uma(10, 30);
            }
            if ("5-10".equals(value)) {
                return new Uma(5, 10);
            }
            if ("なし".equals(value)) {
                return new Uma(0, 0);
            }
            return new Uma(10, 20);
        }

        double valueForRank(int rank) {
            return switch (rank) {
                case 1 -> first;
                case 2 -> second;
                case 3 -> -second;
                case 4 -> -first;
                default -> 0;
            };
        }
    }

    private class ScoreCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            component.setForeground(Color.BLACK);
            if (row == 2) {
                component.setBackground(TOTAL_ROW_COLOR);
                if (column >= 1 && column <= PLAYER_COUNT && value != null && value.toString().startsWith("-")) {
                    component.setForeground(MINUS_COLOR);
                }
            } else if (row >= 3 && row < 3 + GAME_COUNT) {
                component.setBackground((row - 3) % 2 == 0 ? GAME_ROW_COLOR : Color.WHITE);
                if (column >= 1 && column <= PLAYER_COUNT && value != null && value.toString().startsWith("-")) {
                    component.setForeground(MINUS_COLOR);
                }
            } else {
                component.setBackground(Color.WHITE);
            }
            return component;
        }
    }
}
