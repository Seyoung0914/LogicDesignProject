import java.util.*;

public class QuineMcCluskey {

    // Implicant representation
    static class Implicant {
        String mask; // e.g., "0-1-"
        Set<Integer> minterms; // Minterms covered by this implicant (including don't cares)
        boolean checked; // Combined in the current stage

        public Implicant(String mask, Set<Integer> minterms) {
            this.mask = mask;
            this.minterms = new HashSet<>(minterms);
            this.checked = false;
        }

        public int getOneCount() {
            int count = 0;
            for (char c : mask.toCharArray()) {
                if (c == '1') count++;
            }
            return count;
        }

        public Implicant combine(Implicant other) {
            if (this.mask.length() != other.mask.length()) return null;
            int diffCount = 0;
            int diffIdx = -1;
            for (int i = 0; i < mask.length(); i++) {
                char c1 = this.mask.charAt(i);
                char c2 = other.mask.charAt(i);
                if (c1 != c2) {
                    diffCount++;
                    diffIdx = i;
                }
            }
            if (diffCount == 1) {
                StringBuilder sb = new StringBuilder(this.mask);
                sb.setCharAt(diffIdx, '-');
                Set<Integer> combinedMinterms = new HashSet<>(this.minterms);
                combinedMinterms.addAll(other.minterms);
                
                this.checked = true;
                other.checked = true;
                return new Implicant(sb.toString(), combinedMinterms);
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Implicant implicant = (Implicant) o;
            return mask.equals(implicant.mask);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mask);
        }

        @Override
        public String toString() {
            return mask + " " + minterms;
        }
    }

    // Helper to pad binary string to correct bit length
    private static String toBinaryString(int val, int bits) {
        String binary = Integer.toBinaryString(val);
        if (binary.length() > bits) {
            binary = binary.substring(binary.length() - bits);
        }
        while (binary.length() < bits) {
            binary = "0" + binary;
        }
        return binary;
    }

    // Core solver logic
    public static void solve(int bits, List<Integer> minterms, List<Integer> dontCares) {
        // Step 1: Combine terms to find Prime Implicants (PI)
        Set<Integer> allTerms = new HashSet<>(minterms);
        allTerms.addAll(dontCares);

        List<Implicant> currentLevel = new ArrayList<>();
        for (int term : allTerms) {
            Set<Integer> covered = new HashSet<>();
            covered.add(term);
            currentLevel.add(new Implicant(toBinaryString(term, bits), covered));
        }

        Set<Implicant> primeImplicants = new HashSet<>();

        while (true) {
            // Group by number of 1's
            Map<Integer, List<Implicant>> groups = new HashMap<>();
            for (int i = 0; i <= bits; i++) {
                groups.put(i, new ArrayList<>());
            }
            for (Implicant imp : currentLevel) {
                groups.get(imp.getOneCount()).add(imp);
            }

            List<Implicant> nextLevel = new ArrayList<>();
            boolean combinedAny = false;

            // Combine adjacent groups
            for (int i = 0; i < bits; i++) {
                List<Implicant> g1 = groups.get(i);
                List<Implicant> g2 = groups.get(i + 1);
                for (Implicant imp1 : g1) {
                    for (Implicant imp2 : g2) {
                        Implicant combined = imp1.combine(imp2);
                        if (combined != null) {
                            if (!nextLevel.contains(combined)) {
                                nextLevel.add(combined);
                            }
                            combinedAny = true;
                        }
                    }
                }
            }

            // Collect PIs (unchecked implicants in this level)
            for (Implicant imp : currentLevel) {
                if (!imp.checked) {
                    primeImplicants.add(imp);
                }
            }

            if (!combinedAny) {
                break;
            }
            currentLevel = nextLevel;
        }

        // Filter out PIs that contain only don't cares
        List<Implicant> piList = new ArrayList<>();
        for (Implicant pi : primeImplicants) {
            boolean hasActualMinterm = false;
            for (int m : pi.minterms) {
                if (minterms.contains(m)) {
                    hasActualMinterm = true;
                    break;
                }
            }
            if (hasActualMinterm) {
                piList.add(pi);
            }
        }

        System.out.println("Found Prime Implicants (PI):");
        for (int i = 0; i < piList.size(); i++) {
            System.out.println("PI " + i + ": " + piList.get(i).mask + " covering actual minterms: " + getActualMintermsCovered(piList.get(i), minterms));
        }
        System.out.println();

        // Step 2: Answer finding (PI Chart Reduction)
        // Column representation: minterms
        List<Integer> cols = new ArrayList<>(minterms);
        
        // Row representation: PIs
        List<Implicant> rows = new ArrayList<>(piList);

        List<Implicant> epiList = new ArrayList<>();
        List<Implicant> otherSelectedPIs = new ArrayList<>();

        boolean changed = true;
        while (changed) {
            changed = false;

            // 1. EPI 찾기
            for (int colIdx = 0; colIdx < cols.size(); colIdx++) {
                int m = cols.get(colIdx);
                if (m == -1) continue;

                int coverCount = 0;
                int lastCoveringPiIdx = -1;
                for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
                    Implicant pi = rows.get(rowIdx);
                    if (pi == null) continue;
                    if (pi.minterms.contains(m)) {
                        coverCount++;
                        lastCoveringPiIdx = rowIdx;
                    }
                }

                if (coverCount == 1) {
                    Implicant epi = rows.get(lastCoveringPiIdx);
                    epiList.add(epi);
                    System.out.println("Found EPI: " + epi.mask + " (covers minterm " + m + ")");

                    // Remove all minterms covered by this EPI
                    for (int i = 0; i < cols.size(); i++) {
                        int minterm = cols.get(i);
                        if (minterm != -1 && epi.minterms.contains(minterm)) {
                            cols.set(i, -1);
                        }
                    }
                    // Remove this PI from rows
                    rows.set(lastCoveringPiIdx, null);
                    changed = true;
                    break;
                }
            }
            if (changed) continue;

            // 2. 세로비교 (Column Dominance)
            for (int i = 0; i < cols.size(); i++) {
                int m1 = cols.get(i);
                if (m1 == -1) continue;

                for (int j = 0; j < cols.size(); j++) {
                    if (i == j) continue;
                    int m2 = cols.get(j);
                    if (m2 == -1) continue;

                    Set<Integer> piIndices1 = getCoveringPIIndices(m1, rows);
                    Set<Integer> piIndices2 = getCoveringPIIndices(m2, rows);

                    // If piIndices1 contains all elements of piIndices2, then m1 dominates m2.
                    // We remove m1 (A) by setting it to -1.
                    if (piIndices1.containsAll(piIndices2)) {
                        System.out.println("Column Dominance: Minterm " + m1 + " dominates " + m2 + ". Removing minterm " + m1);
                        cols.set(i, -1);
                        changed = true;
                        break;
                    }
                }
                if (changed) break;
            }
            if (changed) continue;

            // 3. 가로비교 (Row Dominance)
            for (int i = 0; i < rows.size(); i++) {
                Implicant pi1 = rows.get(i);
                if (pi1 == null) continue;

                for (int j = 0; j < rows.size(); j++) {
                    if (i == j) continue;
                    Implicant pi2 = rows.get(j);
                    if (pi2 == null) continue;

                    Set<Integer> activeMinterms1 = getActiveMintermsCovered(pi1, cols);
                    Set<Integer> activeMinterms2 = getActiveMintermsCovered(pi2, cols);

                    // If activeMinterms1 contains all elements of activeMinterms2, then pi1 dominates pi2.
                    // We remove pi2 (B) by setting it to null.
                    if (activeMinterms1.containsAll(activeMinterms2)) {
                        System.out.println("Row Dominance: PI " + pi1.mask + " dominates " + pi2.mask + ". Removing PI " + pi2.mask);
                        rows.set(j, null);
                        changed = true;
                        break;
                    }
                }
                if (changed) break;
            }
        }

        // Check for remaining active minterms (Cyclic core)
        List<Integer> remainingCols = new ArrayList<>();
        for (int m : cols) {
            if (m != -1) {
                remainingCols.add(m);
            }
        }

        if (!remainingCols.isEmpty()) {
            System.out.println("Cyclic core detected. Remaining minterms: " + remainingCols);
            List<Implicant> remainingPIs = new ArrayList<>();
            for (Implicant pi : rows) {
                if (pi != null) {
                    remainingPIs.add(pi);
                }
            }

            List<Implicant> optimalCover = solveCyclicCore(remainingCols, remainingPIs);
            System.out.println("Optimal cover for remaining minterms: ");
            for (Implicant pi : optimalCover) {
                System.out.println("- " + pi.mask);
                otherSelectedPIs.add(pi);
            }
        }

        // Step 3: Format and output
        List<Implicant> finalAnswer = new ArrayList<>(epiList);
        for (Implicant pi : otherSelectedPIs) {
            if (!finalAnswer.contains(pi)) {
                finalAnswer.add(pi);
            }
        }

        System.out.println("\n--- Final Result ---");
        if (finalAnswer.isEmpty()) {
            System.out.println("Minimized Formula: 0");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < finalAnswer.size(); i++) {
                sb.append(parsePI(finalAnswer.get(i).mask, bits));
                if (i < finalAnswer.size() - 1) {
                    sb.append(" + ");
                }
            }
            System.out.println("Minimized Formula: " + sb.toString());
        }
    }

    private static Set<Integer> getActualMintermsCovered(Implicant pi, List<Integer> actualMinterms) {
        Set<Integer> result = new HashSet<>();
        for (int m : pi.minterms) {
            if (actualMinterms.contains(m)) {
                result.add(m);
            }
        }
        return result;
    }

    private static Set<Integer> getActiveMintermsCovered(Implicant pi, List<Integer> cols) {
        Set<Integer> result = new HashSet<>();
        for (int m : pi.minterms) {
            if (cols.contains(m)) {
                result.add(m);
            }
        }
        return result;
    }

    private static Set<Integer> getCoveringPIIndices(int minterm, List<Implicant> rows) {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            Implicant pi = rows.get(i);
            if (pi != null && pi.minterms.contains(minterm)) {
                indices.add(i);
            }
        }
        return indices;
    }

    // Backtracking solver for minimum set cover
    private static List<Implicant> solveCyclicCore(List<Integer> minterms, List<Implicant> pis) {
        List<Implicant> bestCover = new ArrayList<>();
        bestCover.addAll(pis);
        
        findBestCoverBacktrack(minterms, pis, 0, new ArrayList<>(), bestCover);
        return bestCover;
    }

    private static void findBestCoverBacktrack(List<Integer> minterms, List<Implicant> pis, int index, 
                                                List<Implicant> current, List<Implicant> bestCover) {
        if (coversAll(current, minterms)) {
            if (current.size() < bestCover.size()) {
                bestCover.clear();
                bestCover.addAll(current);
            }
            return;
        }

        if (current.size() >= bestCover.size() - 1) {
            return;
        }

        if (index == pis.size()) {
            return;
        }

        // Try including pis[index]
        current.add(pis.get(index));
        findBestCoverBacktrack(minterms, pis, index + 1, current, bestCover);
        current.remove(current.size() - 1);

        // Try excluding pis[index]
        findBestCoverBacktrack(minterms, pis, index + 1, current, bestCover);
    }

    private static boolean coversAll(List<Implicant> current, List<Integer> minterms) {
        Set<Integer> covered = new HashSet<>();
        for (Implicant pi : current) {
            covered.addAll(pi.minterms);
        }
        return covered.containsAll(minterms);
    }

    // Parsing PI string representation
    private static String parsePI(String mask, int bits) {
        StringBuilder term = new StringBuilder();
        for (int i = 0; i < bits; i++) {
            char bit = mask.charAt(i);
            if (bit == '-') {
                continue;
            }
            char varChar = (char) ('A' + i);
            term.append(varChar);
            if (bit == '0') {
                term.append("'");
            }
        }
        if (term.length() == 0) {
            return "1";
        }
        return term.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("비트 개수 입력 (e.g. 4): ");
        int bits = scanner.nextInt();

        System.out.print("minterm 개수 입력: ");
        int mintermCount = scanner.nextInt();
        List<Integer> minterms = new ArrayList<>();
        System.out.println("minterm들 입력 (공백으로 구분): ");
        for (int i = 0; i < mintermCount; i++) {
            minterms.add(scanner.nextInt());
        }

        System.out.print("don't care 개수 입력: ");
        int dcCount = scanner.nextInt();
        List<Integer> dontCares = new ArrayList<>();
        if (dcCount > 0) {
            System.out.println("don't care들 입력 (공백으로 구분): ");
            for (int i = 0; i < dcCount; i++) {
                dontCares.add(scanner.nextInt());
            }
        }

        solve(bits, minterms, dontCares);
    }
}
