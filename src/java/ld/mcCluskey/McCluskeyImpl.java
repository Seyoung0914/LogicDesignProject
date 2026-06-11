import java.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quine–McCluskey 알고리즘 구현체.
 *
 * 전체 흐름(solve):
 *   input()     : 비트 수 / minterm / don't care 입력
 *   makePI()    : 인접 항을 반복 병합하여 Prime Implicant(PI) 생성
 *   optimize()  : don't care로만 이루어진 PI 제거
 *   calculate() : PI 차트에서 EPI + dominance 축소 + 남은 항을 골라 최종 답(answer) 도출
 *   print()     : answer를 부울 식 문자열로 변환하여 출력
 */

public class McCluskeyImpl implements McCluskey {

    private final Scanner sc;
    private int bits;               // 비트 개수
    private List<Integer> minterms;   // 출력이 1인 항들
    private List<Integer> dontcares;  // don't care 항들
    private List<PI> primeImplicants; // 최종 PI 리스트
    private List<PI> answer; // 최종 정답 PI 리스트

    private List<PI> rows; // 후보 PI(행)
    private List<List<PI>> columns; // 각 minterm을 덮 PI 목록(열)

    public McCluskeyImpl(Scanner sc) {
        this.sc = sc;
        this.minterms    = new ArrayList<>();
        this.dontcares   = new ArrayList<>();
        this.primeImplicants = new ArrayList<>();
        this.answer      = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
    }

    // 전체 알고리즘의 실행 순서 제어
    @Override
    public void solve() {
        input();
        makePI();
        optimize();
        calculate();
        print();
    }

    // 사용자로부터 비트 수, minterm, don't care를 입력받는다.
    @Override
    public void input() {
        int mintermNum;
        int dontcareNum;

        // 비트 개수 입력
        System.out.print("비트 개수: ");
        bits = sc.nextInt();

        // minterm 개수 입력
        System.out.print("minterm 개수: ");
        mintermNum = sc.nextInt();

        // minterm 값 하나씩 입력
        for (int i = 0; i < mintermNum; i++) {
            System.out.print((i + 1) + "번째 minterm: ");
            minterms.add(sc.nextInt());
        }

        // don't care 개수 입력
        System.out.print("don't care 개수: ");
        dontcareNum = sc.nextInt();

        // don't care 값 하나씩 입력
        for (int i = 0; i < dontcareNum; i++) {
            System.out.print((i + 1) + "번째 don't care: ");
            dontcares.add(sc.nextInt());
         }
    }

    @Override // 입력받은 배열을 간소화하여 primeImplicants 배열 생성
    public void makePI() {
        // minterm과 don't care term 초기 배열에 합함
        ArrayList<Integer> initialTerms = new ArrayList<>(); // 초기 민텀과 돈케어 변수를 합친 하나의 배열
        initialTerms.addAll(minterms);
        initialTerms.addAll(dontcares);
        List<PI> currentPI = initializePI(initialTerms); // 현재 단계 PI 배열 생성

        // 더이상 병합되지 않을 때까지 그룹핑
        while(true) {
            List<List<PI>> oneGroup = grouping(currentPI); // 1의 개수별 그룹
            ArrayList<PI> newPI = new ArrayList<>(); // 현재 단계 PI 배열에서 간소화한 새로운 PI 배열
            boolean merged  = false; // 병합 여부 불리안 변수

            // 인접한 그룹끼리 비교하여 병합 가능 여부 판단
            for (int i = 0; i < oneGroup.size() - 1; i++) {
                for (int j = 0; j < oneGroup.get(i).size(); j++) {
                    for (int k = 0; k < oneGroup.get(i + 1).size(); k++) {
                        if (canMerge(oneGroup.get(i).get(j), oneGroup.get(i + 1).get(k))) {
                            PI a = oneGroup.get(i).get(j);
                            PI b = oneGroup.get(i + 1).get(k);
                            merged = true;

                            PI mergedPI = merge(a, b); // a와 b 합한 새로운 PI 생성

                            // 중복 여부 판단하여 중복일 경우 제외
                            if (!isDuplicate(newPI, mergedPI)) {
                                newPI.add(mergedPI);
                            }

                            // 병합에 사용되었음으로 표시하여 다음 단계 병합에 이어서 사용
                            a.used = true;
                            b.used = true;
                        }
                    }
                }
            }

            // 병합에 사용되지 않은 PI들을 primeImplicants에 추가
            for (PI pi : currentPI) {
                if (!pi.used) primeImplicants.add(pi);
            }

            // 병합이 한 번도 안되었다면 무한루프 탈출
            if (!merged) break;

            // 현재 PI 배열을 갱신
            currentPI = new ArrayList<>(newPI);
        }
    }

    // 초기 PI 리스트 생성
    private List<PI> initializePI(List<Integer> terms) {
        List<PI> result = new ArrayList<>();
        for (int m : terms) {
            String bit = String.format("%" + bits + "s", Integer.toBinaryString(m)).replace(' ', '0');
            result.add(new PI(bit, new ArrayList<>(List.of(m))));
        }
        return result;
    }

    // 두 PI를 병합하여 새 PI 반환
    private PI merge(PI a, PI b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.bit.length(); i++) {
            // a와 b의 각 자리의 데이터를 비교하여 같으면 그대로 추가하고, 다르면 병합시키고 '-' 표기를 이용해 병합되었음을 표기
            sb.append(a.bit.charAt(i) == b.bit.charAt(i) ? a.bit.charAt(i) : '-');
        }

        List<Integer> mergedMinterm = new ArrayList<>(a.minterm);
        for (int m : b.minterm) {
            if (!mergedMinterm.contains(m)) mergedMinterm.add(m);
        }

        return new PI(sb.toString(), mergedMinterm);
    }

    // 비트 패턴 중복 여부 확인
    private boolean isDuplicate(List<PI> newPI, PI mergedPI) {
        for (PI existing : newPI) {
            if (existing.bit.equals(mergedPI.bit)) return true;
        }
        return false;
    }

    // PI 두 개가 병합 가능한지 여부
    private boolean canMerge(PI a, PI b) {
        int different = 0;

        // 비트를 각 자리별로 비교하여 다른 부분이 1개이면 병합 가능, 이외의 경우 병합 불가능
        for (int i = 0; i < a.bit.length(); i++) {
            boolean isAdash = a.bit.charAt(i) == '-';
            boolean isBdash = b.bit.charAt(i) == '-';
            if (isAdash != isBdash) return false;   // 대시 위치 다르면 병합 불가
            if (isAdash && isBdash) continue;       // 둘 다 대시면 그 자리는 통과
            if (a.bit.charAt(i) != b.bit.charAt(i)) {
                different++;
            }
        }
        return different == 1;

    }


    @Override // 1의 개수로 그룹핑하여 반환
    public List<List<PI>> grouping(List<PI> currentPIs) {
        List<List<PI>> groups = new ArrayList<>();
        for (int i = 0; i <= bits; i++) {
            groups.add(new ArrayList<>());
        }

        // PI 클래스의 bit 변수를 순회하며 1의 개수 카운팅
        for (PI pi : currentPIs) {
            int oneCount = 0;
            for (char c : pi.bit.toCharArray()) {
                if (c == '1') oneCount++;
            }
            groups.get(oneCount).add(pi);
        }

        return groups;
    }

    // 돈케어로만 이루어진 PI 제거
    @Override
    public void optimize() {
        for (int i = primeImplicants.size() - 1; i >= 0; i--) {
            PI pi = primeImplicants.get(i);

            int dontcareCount = 0;
            for (int m : pi.minterm) {
                if (dontcares.contains(m)) dontcareCount++;
            }
            // 덮는 항이 모두 don't care면 제거
            if (dontcareCount == pi.minterm.size()) {
                primeImplicants.remove(i);
            }
        }
    }

    // 최종 정답 answer 구하기
    @Override
    public void calculate() {
        // 차트 구성
        buildChart();
        // EPI / 가로비교 / 세로비교를 변화가 없을 때까지 반복
        simplify();
        // 남은 minterm을 가장 많이 덮는 row를 골라 채움
        coverRemaining();
    }

    // 차트 구성
    private void buildChart() {
        rows = new ArrayList<>(primeImplicants);
        columns = new ArrayList<>();
        for (int m : minterms) {
            List<PI> cover = new ArrayList<>();
            for (PI pi : primeImplicants) {
                if (pi.minterm.contains(m)) cover.add(pi);
            }
            columns.add(cover);
        }
    }

    // 간소화하기
    private void simplify() {
        int prev;
        do {
            prev = rows.size() + columns.size();
            findEPI();
            removeRows();
            removeColumns();
        } while (rows.size() + columns.size() != prev);
    }

    // 아직 커버하지 못한 민텀 커버하기
    private void coverRemaining() {
        List<Integer> remaining = new ArrayList<>(minterms);
        for (PI pi : answer) remaining.removeAll(pi.minterm);
        while (!remaining.isEmpty()) {
            PI best = null;
            int bestCount = -1;
            for (PI pi : rows) {
                if (pi == null || answer.contains(pi)) continue;
                int count = 0;
                for (int m : pi.minterm) {
                    if (remaining.contains(m)) count++;
                }
                // 남은 minterm을 가장 많이 덮는 PI 갱신
                if (count > bestCount) {
                    bestCount = count;
                    best = pi;
                }
            }
            if (best == null || bestCount == 0) break;
            answer.add(best);
            remaining.removeAll(best.minterm);
        }
    }

    // 반드시 정답에 포함되어야 하는 항 찾기
    void findEPI() {
        for (int i = columns.size() - 1; i >= 0; i--) {
            if (columns.get(i).size() != 1) continue; // 배열에 요소가 한 개뿐인 민텀을 찾아내기 위한 조건문

            PI epi = columns.get(i).get(0);
            if (!answer.contains(epi)) answer.add(epi);
            rows.remove(epi);

            // EPI가 덮는 minterm 열 전부 제거
            for (int j = columns.size() - 1; j >= 0; j--) {
                if (columns.get(j).contains(epi)) columns.remove(j);
            }
            i = columns.size(); // 차트가 바뀌었으니 처음부터 다시 검사
        }
    }

    // 각 PI들이 커버하고 있는 민텀 집합들 중에 서로 부분집합 관계가 존재할 경우, 더 적은 민텀을 커버하는 PI 제거
    void removeRows() {
        for (int i = 0; i < rows.size(); i++) {
            PI A = rows.get(i);
            if(A == null) continue;

            for (int j = 0; j < rows.size(); j++) {
                PI B = rows.get(j);
                if (i == j||B == null) continue;
                if (A.minterm.containsAll(B.minterm)) { // A가 B를 포함 → B 제거 대상
                    rows.set(j, null);
                }
            }
        }
        // null로 표시된 행 실제 제거
        for(int i = rows.size()-1; i >= 0; i--) {
            if(rows.get(i) == null) rows.remove(i);
        }
    }

    // 각 민텀을 커버하는 PI 집합들 중에 부분집합이 존재할 경우, 더 많은 PI에 의해 커버되는 민텀을 제거
    void removeColumns() {

        // PI 집합들 중 부분집합이 존재할 경우, 더 많은 PI에 의해 커버되는 민텀 제거
        for (int i = columns.size() - 1; i >= 0; i--) {
            for (int j = 0; j < columns.size(); j++) {
                if (i == j) continue;
                if (columns.get(i).containsAll(columns.get(j))) { // A가 B를 포함 → A 제거 대상
                    columns.remove(i);
                    break;
                }
            }
        }
    }

    // 이진수 형태의 문자열로 저장된 데이터를 출력 형식으로 변환
    @Override //
    public String parse() {
        StringBuilder term = new StringBuilder(); // 최종 정답 스트링빌더
        StringBuilder sb = new StringBuilder(); // 임시 스트링빌더

        for (int i = 0; i < answer.size(); i++) {
            for (int j = 0; j < answer.get(i).bit.length(); j++) {
                sb.setLength(0); // 임시 스트링빌더 초기화
                if (answer.get(i).bit.charAt(j) == '1') {
                    sb.append("x");
                    sb.append((j+1));
                    term.append(sb.toString());
                }
                if (answer.get(i).bit.charAt(j) == '0') {
                    sb.append("x");
                    sb.append((j+1));
                    sb.append("'");
                    term.append(sb.toString());
                }
                if (answer.get(i).bit.charAt(j) == '-') {
                    // '-'는 무시한다.
                }
            }
            if (i < answer.size() - 1) {
                term.append(" + "); // pi 사이 + 구분자 삽입
            }
        }
        if (answer.isEmpty()) return "0";   // F=0: 덮을 게 없음
        if (term.isEmpty()) return "1";     // F=1: 전부 don't care 비트
        return term.toString(); //x1x2' + x3x4x5' 형식의 폼으로 변환한 문자열 반환
    }

    // 최종 식을 출력
    @Override
    public void print() {

        System.out.println(parse()); // parsing한 문자열 출ㅇ
    }
}