package com.example.mjscore;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PLAYER_COUNT = 4;
    private static final int GAME_COUNT = 7;
    private static final int GAME_ROW_COLOR = Color.rgb(204, 236, 246);
    private static final int TOTAL_ROW_COLOR = Color.rgb(255, 192, 0);
    private static final int MINUS_COLOR = Color.rgb(190, 30, 45);

    private EditText startPointInput;
    private EditText returnPointInput;
    private Spinner umaSpinner;
    private final TextView[] rankViews = new TextView[PLAYER_COUNT];
    private final TextView[] totalViews = new TextView[PLAYER_COUNT];
    private final EditText[][] scoreInputs = new EditText[GAME_COUNT][PLAYER_COUNT];
    private final double[][] gameScores = new double[GAME_COUNT][PLAYER_COUNT];
    private final boolean[] calculatedGames = new boolean[GAME_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView verticalScroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.setBackgroundColor(Color.rgb(241, 243, 244));
        verticalScroll.addView(root);

        LinearLayout settings = new LinearLayout(this);
        settings.setOrientation(LinearLayout.HORIZONTAL);
        settings.setGravity(Gravity.CENTER_VERTICAL);
        settings.addView(label("持ち点"));
        startPointInput = smallInput("25,000");
        settings.addView(startPointInput);
        settings.addView(label("返し"));
        returnPointInput = smallInput("30,000");
        settings.addView(returnPointInput);
        settings.addView(label("ウマ"));
        umaSpinner = new Spinner(this);
        umaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"10-20", "10-30", "5-10", "なし"}));
        settings.addView(umaSpinner, new LinearLayout.LayoutParams(dp(110), dp(44)));
        root.addView(settings);

        HorizontalScrollView horizontalScroll = new HorizontalScrollView(this);
        GridLayout table = new GridLayout(this);
        table.setColumnCount(PLAYER_COUNT + 1);
        table.setPadding(0, dp(12), 0, 0);
        horizontalScroll.addView(table);
        root.addView(horizontalScroll);

        addTextRow(table, "順位", new String[]{"1位", "2位", "3位", "4位"}, Color.WHITE, true, rankViews);
        addNameRow(table);
        addTextRow(table, "TOTAL", new String[]{"0.0", "0.0", "0.0", "0.0"}, TOTAL_ROW_COLOR, false, totalViews);
        addGameRows(table);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setGravity(Gravity.RIGHT);
        buttons.setPadding(0, dp(18), 0, 0);
        Button resetButton = new Button(this);
        resetButton.setText("リセット");
        resetButton.setOnClickListener(view -> resetAll());
        Button calculateButton = new Button(this);
        calculateButton.setText("全試合を計算");
        calculateButton.setOnClickListener(view -> calculateAll());
        buttons.addView(resetButton);
        buttons.addView(calculateButton);
        root.addView(buttons);

        return verticalScroll;
    }

    private void addNameRow(GridLayout table) {
        table.addView(cell("氏名", Color.WHITE, true));
        for (int player = 0; player < PLAYER_COUNT; player++) {
            EditText input = new EditText(this);
            input.setSingleLine(true);
            input.setGravity(Gravity.CENTER);
            input.setBackgroundColor(Color.WHITE);
            table.addView(input, cellParams());
        }
    }

    private void addGameRows(GridLayout table) {
        for (int game = 0; game < GAME_COUNT; game++) {
            int color = game % 2 == 0 ? GAME_ROW_COLOR : Color.WHITE;
            table.addView(cell(String.valueOf(game + 1), color, false));
            for (int player = 0; player < PLAYER_COUNT; player++) {
                EditText input = new EditText(this);
                input.setSingleLine(true);
                input.setGravity(Gravity.CENTER);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setBackgroundColor(color);
                table.addView(input, cellParams());
                scoreInputs[game][player] = input;
            }
        }
    }

    private void addTextRow(GridLayout table, String label, String[] values, int color, boolean rankRow, TextView[] targets) {
        table.addView(cell(label, color, true));
        for (int i = 0; i < PLAYER_COUNT; i++) {
            TextView view = cell(values[i], color, rankRow);
            table.addView(view);
            targets[i] = view;
        }
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(6), 0, dp(4), 0);
        view.setTextColor(Color.BLACK);
        return view;
    }

    private EditText smallInput(String text) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(text);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.CENTER);
        input.setSelectAllOnFocus(true);
        input.setLayoutParams(new LinearLayout.LayoutParams(dp(96), dp(44)));
        return input;
    }

    private TextView cell(String text, int background, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.BLACK);
        view.setBackgroundColor(background);
        view.setPadding(dp(4), 0, dp(4), 0);
        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        view.setLayoutParams(cellParams());
        return view;
    }

    private GridLayout.LayoutParams cellParams() {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dp(96);
        params.height = dp(44);
        params.setMargins(1, 1, 0, 0);
        return params;
    }

    private void calculateAll() {
        try {
            for (int game = 0; game < GAME_COUNT; game++) {
                calculateGame(game);
            }
            updateTotals();
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void calculateGame(int game) {
        if (calculatedGames[game]) {
            return;
        }

        boolean hasInput = false;
        boolean hasBlank = false;
        int[] points = new int[PLAYER_COUNT];
        for (int player = 0; player < PLAYER_COUNT; player++) {
            String value = scoreInputs[game][player].getText().toString().trim();
            if (value.isEmpty()) {
                hasBlank = true;
                continue;
            }
            hasInput = true;
            points[player] = parseNumber(value, "得点");
        }
        if (!hasInput) {
            return;
        }
        if (hasBlank) {
            throw new IllegalArgumentException((game + 1) + "試合目の得点を4人分入力してください。");
        }

        double[] scores = calculateScores(points);
        gameScores[game] = scores;
        calculatedGames[game] = true;
        for (int player = 0; player < PLAYER_COUNT; player++) {
            scoreInputs[game][player].setText(formatScore(scores[player]));
            scoreInputs[game][player].setTextColor(scores[player] < 0 ? MINUS_COLOR : Color.BLACK);
        }
    }

    private double[] calculateScores(int[] points) {
        int startPoint = parseNumber(startPointInput.getText().toString(), "持ち点");
        int returnPoint = parseNumber(returnPointInput.getText().toString(), "返し");
        double oka = (returnPoint - startPoint) * PLAYER_COUNT / 1000.0;

        List<PlayerResult> results = new ArrayList<>();
        for (int i = 0; i < PLAYER_COUNT; i++) {
            results.add(new PlayerResult(i, points[i]));
        }
        results.sort((a, b) -> Integer.compare(b.point, a.point));

        double[] scores = new double[PLAYER_COUNT];
        for (int rank = 0; rank < results.size(); rank++) {
            PlayerResult result = results.get(rank);
            double score = (result.point - returnPoint) / 1000.0 + umaForRank(rank + 1);
            if (rank == 0) {
                score += oka;
            }
            scores[result.index] = score;
        }
        return scores;
    }

    private void updateTotals() {
        double[] sums = new double[PLAYER_COUNT];
        for (double[] scores : gameScores) {
            for (int player = 0; player < PLAYER_COUNT; player++) {
                sums[player] += scores[player];
            }
        }

        List<PlayerTotal> ranking = new ArrayList<>();
        for (int player = 0; player < PLAYER_COUNT; player++) {
            totalViews[player].setText(formatScore(sums[player]));
            totalViews[player].setTextColor(sums[player] < 0 ? MINUS_COLOR : Color.BLACK);
            ranking.add(new PlayerTotal(player, sums[player]));
        }
        ranking.sort((a, b) -> Double.compare(b.total, a.total));
        for (int rank = 0; rank < ranking.size(); rank++) {
            rankViews[ranking.get(rank).index].setText((rank + 1) + "位");
        }
    }

    private void resetAll() {
        startPointInput.setText("25,000");
        returnPointInput.setText("30,000");
        umaSpinner.setSelection(0);
        for (int game = 0; game < GAME_COUNT; game++) {
            calculatedGames[game] = false;
            for (int player = 0; player < PLAYER_COUNT; player++) {
                gameScores[game][player] = 0;
                scoreInputs[game][player].setText("");
                scoreInputs[game][player].setTextColor(Color.BLACK);
            }
        }
        for (int player = 0; player < PLAYER_COUNT; player++) {
            rankViews[player].setText((player + 1) + "位");
            totalViews[player].setText("0.0");
            totalViews[player].setTextColor(Color.BLACK);
        }
    }

    private double umaForRank(int rank) {
        String value = String.valueOf(umaSpinner.getSelectedItem());
        if ("なし".equals(value)) {
            return 0;
        }
        String[] parts = value.split("-");
        double second = Double.parseDouble(parts[0]);
        double first = Double.parseDouble(parts[1]);
        if (rank == 1) {
            return first;
        }
        if (rank == 2) {
            return second;
        }
        if (rank == 3) {
            return -second;
        }
        if (rank == 4) {
            return -first;
        }
        return 0;
    }

    private int parseNumber(String value, String label) {
        try {
            return Integer.parseInt(value.replace(",", "").trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + "は数値で入力してください。");
        }
    }

    private String formatScore(double score) {
        return String.format("%.1f", score);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class PlayerResult {
        final int index;
        final int point;

        PlayerResult(int index, int point) {
            this.index = index;
            this.point = point;
        }
    }

    private static class PlayerTotal {
        final int index;
        final double total;

        PlayerTotal(int index, double total) {
            this.index = index;
            this.total = total;
        }
    }
}
