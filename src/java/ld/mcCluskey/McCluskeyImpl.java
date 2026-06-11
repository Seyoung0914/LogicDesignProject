import java.util.*;
import java.util.ArrayList;
import java.util.List;


public class McCluskeyImpl implements McCluskey {

    private final Scanner sc;
    private int bits;               // 비트 개수
    private List<Integer> minterms;   // 출력이 1인 항들
    private List<Integer> dontcares;  // don't care 항들
    private List<PI> primeImplicants; // 최종 PI 리스트
    private List<PI> answer;          // 최종 정답 PI 리스트
    private List<PI> rows;
    private List<List<PI>> columns;

    public McCluskeyImpl(Scanner sc) {
        this.sc = sc;
        this.minterms    = new ArrayList<>();
        this.dontcares   = new ArrayList<>();
        this.primeImplicants = new ArrayList<>();
        this.answer      = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
    }

    @Override
    public void solve() {
        /*
         * 전체 알고리즘 실행 순서
         *
         * solve()는 전체 흐름만 제어한다.
         * 각 세부 구현은 아래 함수들에서 나누어 처리한다.
         *
         * Git conflict를 줄이기 위해 solve() 내부는 최대한 수정하지 않는다.
         */
        input();
        makePI();
        optimize();
        calculate();
        print();
    }

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

        // minterm과 don't care term 초기 배열에 합함
        initialTerms.addAll(minterms);
        initialTerms.addAll(dontcares);

        List<PI> currentPI = initializePI(initialTerms); // 현재 단계 PI 배열 생성

        // 더이상 병합되지 않을 때까지 그룹핑
        while(true) {
            List<List<PI>> oneGroup = grouping(currentPI); // 1의 개수별 그룹
            ArrayList<PI> newPI = new ArrayList<>(); // 현재 단계 PI 배열에서 간소화한 새로운 PI 배열
            boolean merged  = false; // 병합 여부 불리안 변수

            // 인접한 그룹의 각 요소를 모두 비교하여 병합 가능 여부 판단
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

                            // 병합에 사용되었음으로 표시하고 다음 병합 단계에서 사용
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

    @Override // 돈케어로만 이루어진 PI 제거
    public void optimize() {
        // 현재 PI 배열 변수를 순회하며 각 PI가 커버하는 민텀 배열과 돈케어 배열이 정확히 일치하면 제거
        for (int i = primeImplicants.size() - 1; i >= 0; i--) {
            int dontcareCount = 0;
            PI pi = primeImplicants.get(i);
            for (int j = 0; j < pi.minterm.size(); j++) {
                for (int k = 0; k < dontcares.size(); k++) {
                    if (pi.minterm.get(j).equals(dontcares.get(k))) {
                        dontcareCount++;
                        break;
                    }
                }
            }
            if (dontcareCount == pi.minterm.size()) {
                primeImplicants.remove(i);
            }
        }
    }

    @Override
    // [두번째 작업: Answer 구하기]
    public void calculate() {
        rows = new ArrayList<>(primeImplicants);
        columns = new ArrayList<>();
        for (int m : minterms) {
            List<PI> cover = new ArrayList<>();
            for (PI pi : primeImplicants) {
                if (pi.minterm.contains(m)) cover.add(pi);
            }
            columns.add(cover);
        }
        // EPI / 가로비교 / 세로비교를 변화가 없을 때까지 반복
        int prev;
        do {
            prev = rows.size() + columns.size();
            findEPI();
            removeRows();
            removeColumns();
        } while (rows.size() + columns.size() != prev);

        // 남은 minterm을 가장 많이 덮는 row를 골라 채움
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
                    if (count > bestCount) {
                        bestCount = count;
                        best = pi;
                    }
                }
            }
            if (best == null || bestCount == 0) break;
            answer.add(best);
            remaining.removeAll(best.minterm);
        }
    }
    void findEPI() {
        for (int i = columns.size() - 1; i >= 0; i--) {
            if (columns.get(i).size() == 1) {
                PI epi = columns.get(i).get(0);

                if (!answer.contains(epi)) answer.add(epi);

                rows.remove(epi);

                // epi가 커버하는 minterm 컬럼 전부 제거
                for (int j = columns.size() - 1; j >= 0; j--) {
                    if (columns.get(j).contains(epi)) columns.remove(j);
                }

                i = columns.size();
            }
        }
    }

    void removeRows() {
//        A. 2중 For문돌린다.
//        i. 바깥 For
        for (int i = 0; i < rows.size(); i++) {
//          1. PI 하나를 정한다 -> A
            PI A = rows.get(i);
            if(A == null) continue;
//          ii. 안쪽 For
            for (int j = 0; j < rows.size(); j++) {
//              1. PI 하나를 정한다 -> B
                PI B = rows.get(j);

//              2. A == B 인 애는 제외해야 한다.
                if (i == j||B == null) continue;

//              3. 안쪽For문에서 정한 if(A.contains(B)) 연산해서 비교한다.
//                  4. If(A.contains(B)) true라면, B를 제거한다. 그렇지 않으면 다음 B값으로 넘어간다.
                if (A.minterm.containsAll(B.minterm)) {
//                  A. 어떻게 지우노? -> NULL로 minterm을 바꾼다.
                    rows.set(j, null);
                }
            }
        }

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
                if (columns.get(i).containsAll(columns.get(j))) {
                    columns.remove(i);
                    break;
                }
            }
        }
    }


    @Override
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

        return term.toString(); //x1x2' + x3x4x5' 형식의 폼으로 변환한 문자열 반환
    }

    @Override
    public void print() {

        System.out.println(parse()); // parsing한 문자열 출ㅇ
    }
}