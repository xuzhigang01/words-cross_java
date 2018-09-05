package code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.Utils.Pair;
import code.WordsBoard.LocatedWord;

public class WordsCrossBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(WordsCrossBuilder.class);

    private static final int WORD_MIN_NUM = 3;
    private static final int WORD_MAX_NUM = 20;
    private static final int WORD_MIN_LEN = 4;
    private static final int BOARD_MAX_WIDTH = 16;
    private static final int BOARD_MAX_HEIGHT = 14;

    public static WordsBoard build(String words) {
        List<String> list = Utils.splitStr(words, "[\\,，]");
        if (list == null || list.size() < WORD_MIN_NUM) {
            throw new IllegalArgumentException("need at lest " + WORD_MIN_NUM + " words");
        } else if (list.size() > WORD_MAX_NUM) {
            throw new IllegalArgumentException("should not more than " + WORD_MAX_NUM + " words");
        }
        List<String> wordList = new ArrayList<>(list.size());
        for (String s : list) {
            if (s.length() < WORD_MIN_LEN) {
                throw new IllegalArgumentException(
                        "invalid word: " + s + ", word should not less than " + WORD_MIN_LEN + " letters");
            }
            wordList.add(s.toUpperCase());
        }

        long ts = System.currentTimeMillis();

        WordsCross cross = new WordsCross(wordList, BOARD_MAX_WIDTH, BOARD_MAX_HEIGHT);
        if (!cross.build()) {
            LOG.debug("build wordsCross faild, cost {}ms", System.currentTimeMillis() - ts);
            return null;
        }
        LOG.debug("build wordsCross OK, cost {}ms\n{}", System.currentTimeMillis() - ts, cross);

        List<LocatedWord> ws = new ArrayList<>();
        for (Pair<String, Position> wp : cross.getWordPositions()) {
            Position p = wp.getRight();

            LocatedWord w = new LocatedWord();
            w.setW(wp.getLeft());
            w.setX(p.x);
            w.setY(p.y);
            w.setD(p.d == Direction.X ? 0 : 1);

            ws.add(w);
        }
        Dimension dim = cross.getSize();

        WordsBoard board = new WordsBoard();
        board.setWidth(dim.width);
        board.setHeight(dim.height);
        board.setWords(ws);

        return board;
    }

    private static class WordsCross {

        private List<String> wordList;
        private int maxWidth;
        private int maxHeight;

        private Cell[][] grids;
        private boolean increX = true;

        private Random rand = new Random();

        private List<Pair<String, Position>> wordPositions;

        public WordsCross(List<String> wordList, int maxWidth, int maxHeight) {
            this.wordList = wordList;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;

            this.wordPositions = new ArrayList<>(wordList.size());
        }

        /** build, return success or not */
        public boolean build() {
            initSize();
            int sn = 0; // success number
            int fn = 0; // fail number
            boolean run = true;
            while (run) {
                if (buildBestCross()) {
                    sn++;
                    // 每个尺寸，尝试100次构建全交叉
                    if (!isAllCrossed() && sn < 100) {
                        continue;
                    }
                    LOG.debug("sn:{}", sn);
                    slimSize();
                    run = false;
                } else {
                    fn++;
                    // 每个尺寸，尝试100次后增加矩阵尺寸
                    if (fn < 100) {
                        continue;
                    }
                    if (!increSize()) {
                        return false;
                    }
                    sn = 0;
                    fn = 0;
                }
            }
            return true;
        }

        public Dimension getSize() {
            return new Dimension(grids[0].length, grids.length);
        }

        public List<Pair<String, Position>> getWordPositions() {
            return wordPositions;
        }

        /** 初始化矩阵尺寸 */
        private void initSize() {
            Dimension dim = getInitSize();
            initGrids(Math.min(dim.width, maxWidth), Math.min(dim.height, maxHeight));
        }

        private Dimension getInitSize() {
            // 字母总数
            int lettersCount = 0;
            for (String w : wordList) {
                lettersCount += w.length();
            }

            // 最长单词长度
            int longestWordLength = 0;
            for (String w : wordList) {
                if (longestWordLength < w.length()) {
                    longestWordLength = w.length();
                }
            }

            // 最短单词长度
            int shortestWordLength = 100;
            for (String w : wordList) {
                if (shortestWordLength > w.length()) {
                    shortestWordLength = w.length();
                }
            }

            int minArea = (int) Math.ceil(lettersCount * 1.33);
            int y = (int) Math.ceil(Math.sqrt(minArea));
            int x = Math.max(longestWordLength, y);
            y = Math.min(y, (int) Math.ceil(minArea / x));
            y = Math.max(y, shortestWordLength);

            return new Dimension(x, y);
        }

        /** 增加矩阵尺寸 */
        private boolean increSize() {
            Dimension dim = getSize();
            if (dim.width >= maxWidth && dim.height >= maxHeight) {
                return false;
            }
            if (increX && dim.width < maxWidth) {
                dim.width++;
            } else if (dim.height < maxHeight) {
                dim.height++;
            } else {
                dim.width++;
            }
            increX = !increX;
            initGrids(dim.width, dim.height);
            return true;
        }

        private void initGrids(int width, int height) {
            LOG.debug("{}x{}", width, height);
            grids = new Cell[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    grids[y][x] = new Cell();
                }
            }
        }

        private void clearGrids() {
            int h = grids.length, w = grids[0].length;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    getCell(x, y).clear();
                }
            }
            wordPositions.clear();
        }

        /** 构建最优的单词交叉布局 */
        private boolean buildBestCross() {
            clearGrids();

            Collections.shuffle(wordList);
            int wordNum = wordList.size();

            String[] words = wordList.toArray(new String[wordNum]);

            boolean[] used = new boolean[wordNum];
            for (int i = 0; i < wordNum; i++) {
                used[i] = false;
            }

            Position[] bestPos = new Position[wordNum];
            int[] scores = new int[wordNum];

            int remaining = wordNum;
            while (remaining > 0) {
                for (int i = 0; i < wordNum; i++) {
                    scores[i] = -1;
                    bestPos[i] = null;
                }
                // 剩下单词的最佳位置及得分
                Pair<Position, Integer> posScore = null;
                for (int i = 0; i < wordNum; i++) {
                    if (used[i] == false && (posScore = getBestPosition(words[i])) != null) {
                        bestPos[i] = posScore.getLeft();
                        scores[i] = posScore.getRight();
                    }
                }

                // 如果最高分的位置有多个，则随机选择一个
                int numbest = 0;
                int bestScore = scores[0];
                for (int i = 0; i < wordNum; i++) {
                    if (scores[i] == bestScore) {
                        numbest++;
                    } else if (scores[i] > bestScore) {
                        bestScore = scores[i];
                        numbest = 1;
                    }
                }
                if (bestScore < 0) {
                    // 一个单词都放不下
                    return false;
                }
                int k = rand.nextInt(numbest);

                numbest = 0;
                int choose = 0;
                for (int i = 0; i < wordNum; i++) {
                    if (scores[i] == bestScore) {
                        if (numbest == k) {
                            choose = i;
                            break;
                        }
                        numbest++;
                    }
                }

                // 放置单词到最佳位置
                placeWord(words[choose], bestPos[choose]);
                used[choose] = true;

                wordPositions.add(Pair.of(words[choose], bestPos[choose]));

                remaining--;
            }

            return true;
        }

        /** 获取最佳位置(及得分)，没有可用的位置返回null */
        private Pair<Position, Integer> getBestPosition(String word) {
            int x = -1;
            int y = -1;
            Direction d = null;

            int score = -1;

            // 遍历每个位置，找出最高分的位置
            int h = grids.length, w = grids[0].length;
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    if (checkPosition(word, xx, yy, Direction.Y)) {
                        int newScore = scorePosition(word, xx, yy, Direction.Y);
                        if (newScore > score) {
                            score = newScore;
                            x = xx;
                            y = yy;
                            d = Direction.Y;
                        }
                    } else if (checkPosition(word, xx, yy, Direction.X)) {
                        int newScore = scorePosition(word, xx, yy, Direction.X);
                        if (newScore > score) {
                            score = newScore;
                            x = xx;
                            y = yy;
                            d = Direction.X;
                        }
                    }
                }
            }
            if (score == -1) {
                // 最后得分为-1，表示放不下此单词
                return null;
            }
            return Pair.of(new Position(x, y, d), score);
        }

        /** 检查位置是否可用 */
        private boolean checkPosition(String word, int x, int y, Direction d) {
            return inGrids(word, x, y, d) && noStart(word, x, y, d) && !havePrePostLetters(word, x, y, d)
                    && !haveAdjacentLetters(word, x, y, d) && crossMatch(word, x, y, d);
        }

        /** 是否在grids范围内 */
        private boolean inGrids(String word, int x, int y, Direction d) {
            int len = word.length();
            if (d == Direction.X) {
                return x + len <= grids[0].length;
            }
            return y + len <= grids.length;
        }

        /** 该区域内，同方向，没有放置其他单词的头字母 */
        private boolean noStart(String word, int x, int y, Direction d) {
            if (d == Direction.X) {
                int right = x + word.length();
                for (int i = x; i < right; i++) {
                    if (getCell(i, y).isStart(d)) {
                        return false;
                    }
                }
            } else {
                int down = y + word.length();
                for (int i = y; i < down; i++) {
                    if (getCell(x, i).isStart(d)) {
                        return false;
                    }
                }
            }
            return true;
        }

        /** 头尾（横向：左右，竖向：上下）是否有字母 */
        private boolean havePrePostLetters(String word, int x, int y, Direction d) {
            int len = word.length();
            if (d == Direction.X) {
                return (x >= 1 && getCell(x - 1, y).content != -1)
                        || (x + len < grids[0].length && getCell(x + len, y).content != -1);
            }
            return (y >= 1 && getCell(x, y - 1).content != -1)
                    || (y + len < grids.length && getCell(x, y + len).content != -1);
        }

        /** 相邻（横向：上下，竖向：左右）是否有字母（非交叉情况） */
        private boolean haveAdjacentLetters(String word, int x, int y, Direction d) {
            if (d == Direction.X) {
                int right = x + word.length();
                int height = grids.length;
                for (int i = x; i < right; i++) {
                    if (getCell(i, y).content == -1 && ((y > 0 && getCell(i, y - 1).content != -1)
                            || (y + 1 < height && getCell(i, y + 1).content != -1))) {
                        return true;
                    }
                }
            } else {
                int down = y + word.length();
                int width = this.grids[0].length;
                for (int j = y; j < down; j++) {
                    if (getCell(x, j).content == -1 && ((x > 0 && getCell(x - 1, j).content != -1)
                            || (x + 1 < width && getCell(x + 1, j).content != -1))) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** 字母是否匹配。如有交叉，检查交叉字母是否匹配 */
        private boolean crossMatch(String word, int x, int y, Direction d) {
            return countCrossedLetters(word, x, y, d) != -1;
        }

        /** 交叉字母数量。交叉字母不匹配返回-1 */
        private int countCrossedLetters(String word, int x, int y, Direction d) {
            int count = 0;

            int len = word.length();
            int c = -1;
            if (d == Direction.X) {
                for (int i = 0; i < len; i++) {
                    c = getCell(x + i, y).content;
                    if (c != -1) {
                        if (c == word.charAt(i)) {
                            count++;
                        } else {
                            return -1;
                        }
                    }
                }
            } else {
                for (int i = 0; i < len; i++) {
                    c = getCell(x, y + i).content;
                    if (c != -1) {
                        if (c == word.charAt(i)) {
                            count++;
                        } else {
                            return -1;
                        }
                    }
                }
            }
            return count;
        }

        /** 给位置打分 */
        private int scorePosition(String word, int x, int y, Direction d) {
            int h = grids.length;
            int w = grids[0].length;
            int len = word.length();
            int count = countCrossedLetters(word, x, y, d);

            // 离中心越近、单词越长、交叉点越多，得分越高；靠边扣分；横向交叉比纵向交叉得分高
            int score = 0;
            if (d == Direction.X) {
                double halfH = (h - 1) / 2;
                score += (y < halfH ? y : h - 1 - y) * Math.round(len / 2);
                if (y == 0 || y == h - 1) {
                    score -= len;
                }
                score += count * 3;
            } else {
                double halfW = (w - 1) / 2;
                score += (x < halfW ? x : w - 1 - x) * Math.round(len / 2);
                if (x == 0 || x == w - 1) {
                    score -= len;
                }
                score += count * 4;
            }
            return score;
        }

        /** 放置单词 */
        private void placeWord(String word, Position pos) {
            int x = pos.x;
            int y = pos.y;
            Direction d = pos.d;

            getCell(x, y).setStart(d);

            word = word.toUpperCase();
            if (d == Direction.X) {
                for (int i = 0; i < word.length(); i++) {
                    if (getCell(x + i, y).content == -1) {
                        getCell(x + i, y).content = word.charAt(i);
                    } else if ((y > 0 && getCell(x + i, y - 1).content != -1)
                            || (y < grids.length - 1 && getCell(x + i, y + 1).content != -1)) {
                        getCell(x + i, y).cross = true;
                    }
                }
            } else {
                for (int i = 0; i < word.length(); i++) {
                    if (getCell(x, y + i).content == -1) {
                        getCell(x, y + i).content = word.charAt(i);
                    } else if ((x > 0 && getCell(x - 1, y + i).content != -1)
                            || (x < grids[0].length - 1 && getCell(x + 1, y + i).content != -1)) {
                        getCell(x, y + i).cross = true;
                    }
                }
            }
        }

        /** 是否全交叉（每个单词都有交叉点） */
        private boolean isAllCrossed() {
            for (Pair<String, Position> wp : wordPositions) {
                boolean cross = false;

                Position p = wp.getRight();
                int len = wp.getLeft().length();

                if (p.d == Direction.X) {
                    for (int i = 0; i < len; i++) {
                        if (getCell(p.x + i, p.y).cross) {
                            cross = true;
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < len; i++) {
                        if (getCell(p.x, p.y + i).cross) {
                            cross = true;
                            break;
                        }
                    }
                }
                if (!cross) {
                    return false;
                }
            }
            return true;
        }

        /** 瘦身：删除空行或空列 */
        private void slimSize() {
            int h = grids.length;
            int w = grids[0].length;

            boolean emptyFirstRow = true;
            for (int i = 0; i < w; i++) {
                if (grids[0][i].content != -1) {
                    emptyFirstRow = false;
                    break;
                }
            }
            if (emptyFirstRow) {
                Cell[][] newGrids = new Cell[h - 1][w];
                for (int y = 0; y < h - 1; y++) {
                    for (int x = 0; x < w; x++) {
                        newGrids[y][x] = grids[y + 1][x];
                    }
                }
                for (Pair<String, Position> wp : wordPositions) {
                    Position p = wp.getRight();
                    p.y = p.y - 1;
                }
                grids = newGrids;
            }

            h = grids.length;
            w = grids[0].length;

            boolean emptyLastRow = true;
            for (int i = 0; i < w; i++) {
                if (grids[h - 1][i].content != -1) {
                    emptyLastRow = false;
                    break;
                }
            }
            if (emptyLastRow) {
                Cell[][] newGrids = new Cell[h - 1][w];
                for (int y = 0; y < h - 1; y++) {
                    for (int x = 0; x < w; x++) {
                        newGrids[y][x] = grids[y][x];
                    }
                }
                grids = newGrids;
            }

            h = grids.length;
            w = grids[0].length;

            boolean emptyFirstColumn = true;
            for (int i = 0; i < h; i++) {
                if (grids[i][0].content != -1) {
                    emptyFirstColumn = false;
                    break;
                }
            }
            if (emptyFirstColumn) {
                Cell[][] newGrids = new Cell[h][w - 1];
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w - 1; x++) {
                        newGrids[y][x] = grids[y][x + 1];
                    }
                }
                for (Pair<String, Position> wp : wordPositions) {
                    Position p = wp.getRight();
                    p.x = p.x - 1;
                }
                grids = newGrids;
            }

            h = grids.length;
            w = grids[0].length;

            boolean emptyLastColumn = true;
            for (int i = 0; i < h; i++) {
                if (grids[i][w - 1].content != -1) {
                    emptyLastColumn = false;
                    break;
                }
            }
            if (emptyLastColumn) {
                Cell[][] newGrids = new Cell[h][w - 1];
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w - 1; x++) {
                        newGrids[y][x] = grids[y][x];
                    }
                }
                grids = newGrids;
            }

            // 继续瘦身
            if (emptyFirstRow || emptyLastRow || emptyFirstColumn || emptyLastColumn) {
                slimSize();
            }
        }

        private Cell getCell(int x, int y) {
            return grids[y][x];
        }

        @Override
        public String toString() {
            Dimension dim = getSize();
            StringBuffer sb = new StringBuffer(30 + dim.width * 2 * (dim.height + 1));
            sb.append("WordsCross: ");
            sb.append(grids[0].length);
            sb.append('x');
            sb.append(grids.length);
            sb.append("\n\n");
            for (int y = 0; y < dim.height; y++) {
                for (int x = 0; x < dim.width; x++) {
                    if (x > 0) {
                        sb.append(' ');
                    }
                    int c = getCell(x, y).content;
                    sb.append(c != -1 ? (char) c : '_');
                }
                sb.append('\n');
            }
            return sb.toString();
        }

        static class Cell {
            int content = -1;
            boolean cross;
            private boolean startX;
            private boolean startY;

            void setStart(Direction d) {
                if (d == Direction.X) {
                    startX = true;
                } else {
                    startY = true;
                }
            }

            boolean isStart(Direction d) {
                return d == Direction.X ? startX : startY;
            }

            void clear() {
                content = -1;
                cross = false;
                startX = false;
                startY = false;
            }
        }
    }

    static class Dimension {
        int width;
        int height;

        public Dimension(int w, int h) {
            width = w;
            height = h;
        }
    }

    static enum Direction {
        X, Y
    }

    static class Position {
        int x = -1;
        int y = -1;
        Direction d;

        public Position(int x, int y, Direction d) {
            this.x = x;
            this.y = y;
            this.d = d;
        }
    }
}
