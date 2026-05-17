const playerCount = 4;
const gameCount = 7;
const games = document.querySelector("#games");
const startPoint = document.querySelector("#startPoint");
const returnPoint = document.querySelector("#returnPoint");
const umaSelect = document.querySelector("#uma");
const totals = [...document.querySelectorAll(".total")];
const ranks = [...document.querySelectorAll(".rank")];
const calculateButton = document.querySelector("#calculateButton");
const resetButton = document.querySelector("#resetButton");

let gameScores = Array.from({ length: gameCount }, () => Array(playerCount).fill(0));
let calculatedGames = Array(gameCount).fill(false);

for (let game = 0; game < gameCount; game++) {
  const row = document.createElement("div");
  row.className = "grid row game-row";
  row.dataset.game = String(game);
  row.innerHTML = `<div class="cell label">${game + 1}</div>` +
    Array.from({ length: playerCount }, (_, player) =>
      `<div class="cell"><input inputmode="numeric" aria-label="${game + 1}試合目 ${player + 1}人目の得点"></div>`
    ).join("");
  games.append(row);
}

function parseNumber(value, label) {
  const normalized = String(value).replaceAll(",", "").trim();
  if (!/^-?\d+$/.test(normalized)) {
    throw new Error(`${label}は数値で入力してください。`);
  }
  return Number(normalized);
}

function formatPoint(value) {
  return new Intl.NumberFormat("ja-JP").format(value);
}

function formatScore(value) {
  return value.toFixed(1);
}

function umaValue(rank) {
  const value = umaSelect.value;
  if (value === "none") {
    return 0;
  }
  const [second, first] = value.split("-").map(Number);
  if (rank === 1) return first;
  if (rank === 2) return second;
  if (rank === 3) return -second;
  return -first;
}

function calculateGame(gameIndex) {
  if (calculatedGames[gameIndex]) {
    return false;
  }

  const row = games.querySelector(`[data-game="${gameIndex}"]`);
  const inputs = [...row.querySelectorAll("input")];
  if (inputs.every(input => input.value.trim() === "")) {
    return false;
  }
  if (inputs.some(input => input.value.trim() === "")) {
    throw new Error(`${gameIndex + 1}試合目の得点を4人分入力してください。`);
  }

  const points = inputs.map((input, index) => {
    const point = parseNumber(input.value, `${index + 1}人目の得点`);
    input.value = formatPoint(point);
    return point;
  });

  const start = parseNumber(startPoint.value, "持ち点");
  const returned = parseNumber(returnPoint.value, "返し");
  const oka = (returned - start) * playerCount / 1000;
  const order = points
    .map((point, index) => ({ point, index }))
    .sort((a, b) => b.point - a.point);

  const scores = Array(playerCount).fill(0);
  order.forEach((result, index) => {
    const rank = index + 1;
    let score = (result.point - returned) / 1000 + umaValue(rank);
    if (rank === 1) {
      score += oka;
    }
    scores[result.index] = score;
  });

  gameScores[gameIndex] = scores;
  calculatedGames[gameIndex] = true;
  inputs.forEach((input, index) => {
    input.value = formatScore(scores[index]);
    input.classList.toggle("minus", scores[index] < 0);
  });
  return true;
}

function updateTotals() {
  const sums = Array(playerCount).fill(0);
  gameScores.forEach(scores => {
    scores.forEach((score, index) => {
      sums[index] += score;
    });
  });

  sums.forEach((sum, index) => {
    totals[index].textContent = formatScore(sum);
    totals[index].classList.toggle("minus", sum < 0);
  });

  sums
    .map((sum, index) => ({ sum, index }))
    .sort((a, b) => b.sum - a.sum)
    .forEach((result, index) => {
      ranks[result.index].textContent = `${index + 1}位`;
    });
}

function calculateAll() {
  try {
    for (let game = 0; game < gameCount; game++) {
      calculateGame(game);
    }
    updateTotals();
  } catch (error) {
    alert(error.message);
  }
}

function resetAll() {
  startPoint.value = "25,000";
  returnPoint.value = "30,000";
  umaSelect.value = "10-20";
  gameScores = Array.from({ length: gameCount }, () => Array(playerCount).fill(0));
  calculatedGames = Array(gameCount).fill(false);
  document.querySelectorAll(".game-row input").forEach(input => {
    input.value = "";
    input.classList.remove("minus");
  });
  document.querySelectorAll(".name").forEach(input => {
    input.value = "";
  });
  totals.forEach(total => {
    total.textContent = "0.0";
    total.classList.remove("minus");
  });
  ranks.forEach((rank, index) => {
    rank.textContent = `${index + 1}位`;
  });
}

calculateButton.addEventListener("click", calculateAll);
resetButton.addEventListener("click", resetAll);

if ("serviceWorker" in navigator) {
  navigator.serviceWorker.register("./sw.js").catch(() => {});
}
