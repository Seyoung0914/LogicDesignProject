import java.util.*;
import java.util.ArrayList;
import java.util.List;


public class McCluskeyImpl implements McCluskey {

    private final Scanner sc;

    int bits;               // 비트 개수
    List<Integer> minterms;   // 출력이 1인 항들
    List<Integer> dontcares;  // don't care 항들
    List<PI> primeImplicants; // 최종 PI 리스트
    List<PI> answer;          // 최종 정답 PI 리스트

    public McCluskeyImpl(Scanner sc) {
        this.sc = sc;
        this.minterms    = new ArrayList<>();
        this.dontcares   = new ArrayList<>();
        this.primeImplicants = new ArrayList<>();
        this.answer      = new ArrayList<>();
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
//        input();
//        makePI();
//        optimize();
//        calculate();
//        print();
    }

    @Override   
    public void input() {
        int bCount;              
        int mNum;             
        int dcNum;            
        ArrayList<Integer> mintermList  = new ArrayList<>();
        ArrayList<Integer> dontCareList = new ArrayList<>();

        // 비트 개수 입력
        System.out.print("비트 개수: ");
        bCount = sc.nextInt();

        // minterm 개수 입력
        System.out.print("minterm 개수: ");
        mNum = sc.nextInt();

        // minterm 값 하나씩 입력
        for (int i = 0; i < mNum; i++) {
            System.out.print((i + 1) + "번째 minterm: ");
            mintermList.add(sc.nextInt());
        }

        // don't care 개수 입력
        System.out.print("don't care 개수: ");
        dcNum = sc.nextInt();

        // don't care 값 하나씩 입력
        for (int i = 0; i < dcNum; i++) {
            System.out.print((i + 1) + "번째 don't care: ");
            dontCareList.add(sc.nextInt());
         }
    }

    @Override
    public ArrayList<PI> makePI(ArrayList<Integer> minterm, ArrayList<Integer> dontcare) {
        /*

         * 3. 인접한 그룹끼리 비교한다.
         *    예: 1의 개수가 1개인 그룹과 2개인 그룹을 비교한다.
         *
         * 4. 1비트만 다른 PI끼리 묶는다.
         *    예: 0001과 0011은 1비트만 다르므로 00-1로 묶을 수 있다.
         *
         * 5. 묶인 PI는 checked 처리한다.
         *
         * 6. 끝까지 묶이지 않은 PI는 prime implicant 리스트에 넣는다.
         *
         * 7. 더 이상 새롭게 묶이는 PI가 없으면 반복을 종료한다.
         */

        // minterm과 don't care term 초기 배열에 합함
        ArrayList<Integer> initialPI = new ArrayList<>();
        ArrayList<PI> currentPI = new ArrayList<>();
        ArrayList<PI> newPI = new ArrayList<>();
        ArrayList<PI> primeImplicants = new ArrayList<>();
        initialPI.addAll(minterm);
        initialPI.addAll(dontcare);

        // PI 생성자 생성 후 currentPI에 추가
        for (int m : initialPI) {
            String bit = String.format("%4s", Integer.toBinaryString(m)).replace(' ', '0');
            PI pi = new PI(bit, List.of(m));
            currentPI.add(pi);
        }

        while(true) {
            List<List<PI>> oneGroup = grouping(currentPI);
            newPI = new ArrayList<>();
            boolean merged  = false;

            for (int i = 0; i < oneGroup.size() - 1; i++) {
                for (int j = 0; j < oneGroup.get(i).size(); j++) {
                    for (int k = 0; k < oneGroup.get(i + 1).size(); k++) {
                        if (canMerge(oneGroup.get(i).get(j), oneGroup.get(i + 1).get(k))) {
                            PI a = oneGroup.get(i).get(j);
                            PI b = oneGroup.get(i + 1).get(k);
                            merged = true;

                            // 다른 자리를 -로 교체
                            StringBuilder sb = new StringBuilder();
                            for (int idx = 0; idx < a.bit.length(); idx++) {
                                if (a.bit.charAt(idx) == b.bit.charAt(idx)) sb.append(a.bit.charAt(idx));
                                else sb.append('-');
                            }

                            // minterm 합치기
                            List<Integer> mergedMinterm = new ArrayList<>(a.minterm);
                            for (int m : b.minterm) {
                                if (!mergedMinterm.contains(m)) mergedMinterm.add(m);
                            }

                            // 합쳤을 떄 중복 PI인지 검사
                            boolean alreadyExists = false;
                            for (PI existing : newPI) {
                                if (existing.bit.equals(sb.toString())) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            a.used = true;
                            b.used = true;

                            if (!alreadyExists) {
                                newPI.add(new PI(sb.toString(), mergedMinterm));
                            }

                        }
                    }
                }
            }

            for (PI pi : currentPI) {
                if (!pi.used) primeImplicants.add(pi);
            }

            if (!merged) break;
            currentPI = new ArrayList<>(newPI);
        }

        return primeImplicants;
    }

    // PI 두 개가 병합 가능한지 여부
    public boolean canMerge(PI a, PI b) {
        int different = 0;
        for (int i = 0; i < a.bit.length(); i++) {
            if (a.bit.charAt(i) != b.bit.charAt(i)) {
                different++;
            }
        }
        return different == 1;

    }


    @Override
    public List<List<PI>> grouping(List<PI> currentPIs) {
        /*
         * [그룹핑]
         *
         * currentPIs에 들어 있는 PI들을 1의 개수 기준으로 나눈다.
         *
         * 예:
         * 0001 -> 1의 개수 1개
         * 0011 -> 1의 개수 2개
         * 0111 -> 1의 개수 3개
         *
         * 결과 구조:
         * groups[0] = 1의 개수가 0개인 PI들
         * groups[1] = 1의 개수가 1개인 PI들
         * groups[2] = 1의 개수가 2개인 PI들
         */
        List<List<PI>> groups = new ArrayList<>();
        for (int i = 0; i <= currentPIs.get(0).bit.length(); i++) {
            groups.add(new ArrayList<>());
        }

        for (PI pi : currentPIs) {
            int oneCount = 0;
            for (char c : pi.bit.toCharArray()) {
                if (c == '1') {
                    oneCount++;
                }
            }
            groups.get(oneCount).add(pi);
        }

        return groups;
    }

    @Override
    public ArrayList<PI> optimize(ArrayList<PI> currentPIs, ArrayList<Integer> dontcare) {
        // [중간 작업: don't care 제거 및 table 준비]
        //
        // 1. PI 중에서 don't care만 포함한 PI를 제거한다.
        //  자 일단 받아. PI리스트랑 dont care 리스트를 받아
        //  그리고 비교해 만약에 PI리스트에 dont care가 포함되면 그 다음 PI를 봐 근데 또 포함돼? 그러면
        //  끝이야. 이걸 한번에 비교할 수는 없나?
        for (int i = currentPIs.size() - 1; i >= 0; i--) {
            int dontcareCount = 0;
            PI pi = currentPIs.get(i);
            for (int j = 0; j < pi.minterm.size(); j++) {
                for (int k = 0; k < dontcare.size(); k++) {
                    if (pi.minterm.get(j).equals(dontcare.get(k))) {
                        dontcareCount++;
                        break;
                    }
                }
            }
            if (dontcareCount == pi.minterm.size()) {
                currentPIs.remove(i);
            }
        }
     //    예:
     //    PI가 {2, 6}을 포함하고,
     //    2와 6이 모두 don't care라면 제거한다.
     //
     //    하지만 PI가 {1, 5}를 포함하고,
     //    1은 minterm, 5는 don't care라면 제거하면 안 된다.
     //
     // 2. row, column 구조를 만든다.
     //
     //    row    = PI 기준
     //    column = minterm 기준
     //
     // 3. 각 minterm을 어떤 PI가 커버하는지 저장한다.
     //
     //    예:
     //    minterm 1 -> PI1, PI3
     //    minterm 3 -> PI1
     //    minterm 5 -> PI2, PI3
        return currentPIs;
    }

    @Override
    public void calculate() {
        /*
         * [두번째 작업: Answer 구하기]
         *
         * 1. Answer 리스트를 비워둔다.
         *  void findEPI();
         *  void removeRows에는();
         *  void removeColumns에는();
         *  void findPI();
         * 2. removeRows에는 PI들을 저장한다.
         *    의미: 각 PI가 어떤 minterm들을 커버하는지 저장한다.
         *
         * 3. removeColumns에는 minterm들을 저장한다.
         *    의미: 각 minterm을 어떤 PI들이 커버하는지 저장한다.
         *
         * 4. EPI를 찾는다.
         *    어떤 minterm을 커버하는 PI가 하나뿐이면 그 PI는 EPI이다.
         *
         * 5. 세로 비교를 수행한다.
         *    minterm column끼리 비교해서 불필요한 column을 제거한다.
         *
         * 6. 가로 비교를 수행한다.
         *    PI row끼리 비교해서 불필요한 PI를 제거한다.
         *
         * 7. 남은 minterm을 커버하는 PI를 최종 answer에 추가한다.
         */
    }
    void findEPI() {

        for (int m : minterms) {
            int count = 0;
            for (PI pi : primeImplicants) {
                if (pi.minterm.contains(m)) {
                    count++;
                    targetPI = pi; 
                }
            }

            if (count == 1 && !answer.contains(targetPI)) {
                answer.add(targetPI);
            }

    }
    
    }
    void removeRows() {

        Map<PI, List<Integer>> rows = new HashMap<>();


//        A. 2중 For문돌린다.
//        i. 바깥 For
//        1. PI 하나를 정한다 -> A
//        ii. 안쪽 For
//        1. PI 하나를 정한다 -> B
//        2. A == B 인 애는 제외해야 한다.
//        3. 안쪽For문에서 정한 if(A.contains(B)) 연산해서 비교한다.
//        4. If(A.contains(B)) true라면, B를 제거한다. 그렇지 않으면 다음 B값으로 넘어간다.
//                A. 어떻게 지우노? -> NULL로 minterm을 바꾼다.

        List<PI> rows = new ArrayList<>(primeImplicants);

        for (int i = 0; i < rows.size(); i++) {
            PI A = rows.get(i);

            if (A == null) {
                continue;
            }

            for (int j = 0; j < rows.size(); j++) {
                PI B = rows.get(j);

                if (i == j) {
                    continue;
                }

                if (B == null) {
                    continue;
                }

                if (A.minterm.containsAll(B.minterm)) {
                    rows.set(j, null);
                }
            }
        }

        rows.removeIf(pi -> pi == null);

        primeImplicants = rows;
    }
    List<List<PI>> removeColumns() {
        List<List<PI>> columns = new ArrayList<>();

        for (int m : minterms) {
            List<PI> cover = new ArrayList<>();
            for (PI pi : primeImplicants) {
                if (pi.minterm.contains(m)) cover.add(pi);
            }
            columns.add(cover);
        }

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
    void findPI() {

        // 1. EPI 찾기
        findEPI();

        // [정리] EPI가 덮은 minterm은 더 이상 볼 필요 없으니 -1로 마킹하여 지움
        for (PI epi : answer) {
            for (int i = 0; i < minterms.size(); i++) {
                if (epi.minterm.contains(minterms.get(i))) {
                    minterms.set(i, -1); 
                }
            }
        }

        // 2. 세로 비교 (Minterm 열 지배 규칙)
        for (int i = 0; i < minterms.size(); i++) {
            int mA = minterms.get(i);
            if (mA == -1) continue; 

            // Minterm A를 커버하는 PI 목록 수집
            List<PI> piListA = new ArrayList<>();
            for (PI pi : primeImplicants) {
                if (pi != null && pi.minterm.contains(mA)) piListA.add(pi);
            }

            for (int j = 0; j < minterms.size(); j++) {
                if (i == j) continue;
                int mB = minterms.get(j);
                if (mB == -1) continue;

                // Minterm B를 커버하는 PI 목록 수집
                List<PI> piListB = new ArrayList<>();
                for (PI pi : primeImplicants) {
                    if (pi != null && pi.minterm.contains(mB)) piListB.add(pi);
                }

                // [핵심] A가 B를 모두 포함(지배)하면? 
                // -> B를 덮는 PI를 선택하면 A는 무조건 덮이므로, 더 큰(조건이 널널한) A 열을 지운다.
                if (piListA.containsAll(piListB)) {
                    minterms.set(i, -1);
                    break;
                }
            }
        }

        // 3. 가로 비교 (PI 행 지배 규칙)
        for (int i = 0; i < primeImplicants.size(); i++) {
            PI piA = primeImplicants.get(i);
            if (piA == null) continue;

            for (int j = 0; j < primeImplicants.size(); j++) {
                if (i == j) continue;
                PI piB = primeImplicants.get(j);
                if (piB == null) continue;

                // [핵심] PI A가 PI B의 minterm을 모두 포함(대체)할 수 있다면?
                // -> B는 A의 하위호환이므로, 쓸모없는 B 행을 지운다.
                if (piA.minterm.containsAll(piB.minterm)) {
                    primeImplicants.set(j, null);
                }
            }
        }

        // 4. 남은 유효 PI 수거 (정답에 추가)
        for (PI pi : primeImplicants) {
            if (pi != null && !answer.contains(pi)) {
                for (int m : pi.minterm) {
                    // -1로 지워지지 않고 배열에 살아남은 minterm을 하나라도 덮는다면 정답에 추가
                    if (minterms.contains(m)) {
                        answer.add(pi);
                        break; 
                    }
                }
            }
        }
    }

    @Override
    public boolean isDontcare(int minterm) {
        /*
         * [don't care 확인]
         *
         * 특정 minterm 번호가 don't care 목록에 포함되어 있는지 확인한다.
         *
         * 포함되어 있으면 true
         * 포함되어 있지 않으면 false
         */
        return false;
    }

    @Override
    public String parse(PI pi) {
        /*
         * [PI를 문자식으로 변환]
         *
         * PI의 bit 표현은 0, 1, - 로 구성된다.
         *
         * 예:
         * bits = "10-1"
         *
         * 변환 규칙:
         * 1. '-'는 해당 변수를 무시한다.
         * 2. '1'이면 x를 그대로 출력한다.
         * 3. '0'이면 x에 '를 붙인다.
         *
         * 예:
         * 10-1
         * x1 x2' x4
         *
         * 결과:
         * x1x2'x4
         */
        return "";
    }

    @Override
    public void print() {
        /*
         * [출력하기]
         *
         * 1. answer 리스트를 순회한다.
         * 2. 각 PI를 parse()로 문자식으로 바꾼다.
         * 3. 최종 논리식을 출력한다.
         *
         * 예:
         * x1'x2 + x3x4'
         */
    }
}