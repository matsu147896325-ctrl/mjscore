import SwiftUI

struct ContentView: View {
    private let playerCount = 4
    private let gameCount = 7
    private let umaOptions = ["10-20", "10-30", "5-10", "なし"]

    @State private var startPoint = "25,000"
    @State private var returnPoint = "30,000"
    @State private var selectedUma = "10-20"
    @State private var names = Array(repeating: "", count: 4)
    @State private var ranks = ["1位", "2位", "3位", "4位"]
    @State private var totals = Array(repeating: 0.0, count: 4)
    @State private var scoreInputs = Array(repeating: Array(repeating: "", count: 4), count: 7)
    @State private var gameScores = Array(repeating: Array(repeating: 0.0, count: 4), count: 7)
    @State private var calculatedGames = Array(repeating: false, count: 7)
    @State private var alertMessage = ""
    @State private var showsAlert = false

    var body: some View {
        VStack(spacing: 12) {
            settings
            ScrollView([.horizontal, .vertical]) {
                VStack(spacing: 0) {
                    row(label: "順位", values: ranks)
                    nameRow
                    row(label: "TOTAL", values: totals.map(formatScore), background: Color(red: 1, green: 0.75, blue: 0))
                    ForEach(0..<gameCount, id: \.self) { game in
                        gameRow(game)
                    }
                }
                .overlay(Rectangle().stroke(Color.gray, lineWidth: 1))
            }
            HStack {
                Spacer()
                Button("リセット", action: resetAll)
                Button("全試合を計算", action: calculateAll)
                    .fontWeight(.bold)
            }
        }
        .padding()
        .background(Color(red: 0.95, green: 0.96, blue: 0.96))
        .alert("入力エラー", isPresented: $showsAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
    }

    private var settings: some View {
        HStack {
            labeledInput("持ち点", text: $startPoint)
            labeledInput("返し", text: $returnPoint)
            Picker("ウマ", selection: $selectedUma) {
                ForEach(umaOptions, id: \.self) { option in
                    Text(option)
                }
            }
            .pickerStyle(.menu)
        }
    }

    private func labeledInput(_ label: String, text: Binding<String>) -> some View {
        HStack(spacing: 4) {
            Text(label).fontWeight(.bold)
            TextField("", text: text)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.numbersAndPunctuation)
                .frame(width: 95)
        }
    }

    private var nameRow: some View {
        HStack(spacing: 0) {
            cell("氏名")
            ForEach(0..<playerCount, id: \.self) { player in
                TextField("", text: $names[player])
                    .multilineTextAlignment(.center)
                    .frame(width: 96, height: 40)
                    .background(Color.white)
                    .overlay(Rectangle().stroke(Color.gray, lineWidth: 0.5))
            }
        }
    }

    private func row(label: String, values: [String], background: Color = .white) -> some View {
        HStack(spacing: 0) {
            cell(label, background: background, bold: true)
            ForEach(0..<playerCount, id: \.self) { player in
                cell(values[player], background: background, foreground: values[player].hasPrefix("-") ? .red : .black)
            }
        }
    }

    private func gameRow(_ game: Int) -> some View {
        let background = game.isMultiple(of: 2) ? Color(red: 0.8, green: 0.93, blue: 0.96) : Color.white
        return HStack(spacing: 0) {
            cell("\(game + 1)", background: background)
            ForEach(0..<playerCount, id: \.self) { player in
                TextField("", text: $scoreInputs[game][player])
                    .keyboardType(.numbersAndPunctuation)
                    .multilineTextAlignment(.center)
                    .foregroundColor(scoreInputs[game][player].hasPrefix("-") ? .red : .black)
                    .frame(width: 96, height: 40)
                    .background(background)
                    .overlay(Rectangle().stroke(Color.gray, lineWidth: 0.5))
            }
        }
    }

    private func cell(_ text: String, background: Color = .white, foreground: Color = .black, bold: Bool = false) -> some View {
        Text(text)
            .fontWeight(bold ? .bold : .regular)
            .foregroundColor(foreground)
            .frame(width: 96, height: 40)
            .background(background)
            .overlay(Rectangle().stroke(Color.gray, lineWidth: 0.5))
    }

    private func calculateAll() {
        do {
            for game in 0..<gameCount {
                try calculateGame(game)
            }
            updateTotals()
        } catch {
            alertMessage = error.localizedDescription
            showsAlert = true
        }
    }

    private func calculateGame(_ game: Int) throws {
        if calculatedGames[game] {
            return
        }
        let values = scoreInputs[game]
        if values.allSatisfy({ $0.trimmingCharacters(in: .whitespaces).isEmpty }) {
            return
        }
        if values.contains(where: { $0.trimmingCharacters(in: .whitespaces).isEmpty }) {
            throw ScoreError.message("\(game + 1)試合目の得点を4人分入力してください。")
        }

        let points = try values.map { try parseNumber($0, label: "得点") }
        let scores = try calculateScores(points)
        gameScores[game] = scores
        calculatedGames[game] = true
        for player in 0..<playerCount {
            scoreInputs[game][player] = formatScore(scores[player])
        }
    }

    private func calculateScores(_ points: [Int]) throws -> [Double] {
        let start = try parseNumber(startPoint, label: "持ち点")
        let returned = try parseNumber(returnPoint, label: "返し")
        let oka = Double((returned - start) * playerCount) / 1000.0
        let order = points.enumerated().sorted { $0.element > $1.element }
        var scores = Array(repeating: 0.0, count: playerCount)
        for (index, result) in order.enumerated() {
            let rank = index + 1
            var score = Double(result.element - returned) / 1000.0 + uma(rank)
            if rank == 1 {
                score += oka
            }
            scores[result.offset] = score
        }
        return scores
    }

    private func updateTotals() {
        totals = Array(repeating: 0.0, count: playerCount)
        for game in 0..<gameCount {
            for player in 0..<playerCount {
                totals[player] += gameScores[game][player]
            }
        }
        for (rank, result) in totals.enumerated().sorted(by: { $0.element > $1.element }).enumerated() {
            ranks[result.offset] = "\(rank + 1)位"
        }
    }

    private func resetAll() {
        startPoint = "25,000"
        returnPoint = "30,000"
        selectedUma = "10-20"
        names = Array(repeating: "", count: 4)
        ranks = ["1位", "2位", "3位", "4位"]
        totals = Array(repeating: 0.0, count: 4)
        scoreInputs = Array(repeating: Array(repeating: "", count: 4), count: 7)
        gameScores = Array(repeating: Array(repeating: 0.0, count: 4), count: 7)
        calculatedGames = Array(repeating: false, count: 7)
    }

    private func uma(_ rank: Int) -> Double {
        if selectedUma == "なし" {
            return 0
        }
        let parts = selectedUma.split(separator: "-").compactMap { Double($0) }
        let second = parts[0]
        let first = parts[1]
        if rank == 1 { return first }
        if rank == 2 { return second }
        if rank == 3 { return -second }
        return -first
    }

    private func parseNumber(_ value: String, label: String) throws -> Int {
        let normalized = value.replacingOccurrences(of: ",", with: "").trimmingCharacters(in: .whitespaces)
        guard let number = Int(normalized) else {
            throw ScoreError.message("\(label)は数値で入力してください。")
        }
        return number
    }

    private func formatScore(_ value: Double) -> String {
        String(format: "%.1f", value)
    }
}

enum ScoreError: LocalizedError {
    case message(String)

    var errorDescription: String? {
        switch self {
        case .message(let message):
            return message
        }
    }
}
