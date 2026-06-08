import java.util.*;

/**
 * QuineMcCluskeyAlgorithm 인터페이스
 * 논리 최적화 알고리즘(Quine-McCluskey)을 구현하기 위해 정의된 규격(인터페이스)입니다.
 */
interface QuineMcCluskeyAlgorithm {
    void input();                                     // 사용자로부터 초기 데이터를 입력받는 함수
    void makePI();                                    // 인접 항들을 비교하고 묶어서 주항(Prime Implicants, PI)을 만드는 함수
    List<List<PI>> grouping(List<PI> currentPIs);     // 주항들을 2진수 내 '1'의 개수에 따라 그룹별로 분류하는 함수
    void optimize();                                  // don't care로만 이루어진 불필요한 항을 제거하고 표(가로, 세로)를 세팅하는 함수
    void calculate();                                 // 필수 주항(EPI)을 찾고, 남은 항들을 탐욕법(Greedy)으로 골라 최종 정답을 구하는 함수
    boolean isDontcare(int minterm);                  // 특정 숫자가 don't care인지 확인하는 함수
    String parse(PI pi);                              // 2진수 형태의 주항("10-")을 변수 기호("x1x2'") 형태로 가독성 있게 번환하는 함수
    void print();                                     // 최적화된 식들을 '+'로 묶어 최종 출력하는 함수
}

/**
 * PI (Prime Implicant) 클래스
 * 2진수 패턴(예: "01-1")과 이 패턴이 커버하는 원래의 10진수 숫자들(minterm 목록)을 묶어서 저장하는 데이터 객체입니다.
 */
class PI {
    String bit;             // 2진수 문자열 표현 (예: "010", "1-0" 등)
    List<Integer> minterm;  // 이 패턴이 커버하는 원래의 10진수 숫자 목록 (예: [2, 6])

    // 생성자: 객체를 처음 생성할 때 값을 채워넣는 역할
    public PI(String bit, List<Integer> minterm) {
        this.bit = bit;
        this.minterm = minterm;
    }
}

/**
 * LogicOptimizer 클래스 (Quine-McCluskey 알고리즘 구현체)
 */
public class LogicOptimizer implements QuineMcCluskeyAlgorithm {
    
    int bitCount;                              // 입력된 변수(비트)의 개수 (예: 5비트 회로이면 5)
    List<Integer> minterms = new ArrayList<>(); // 반드시 출력이 1(True)이 되어야 하는 10진수 숫자 목록 (minterm)
    List<Integer> dontcares = new ArrayList<>();// 출력이 0이든 1이든 상관없는 10진수 숫자 목록 (don't care)
    
    List<PI> piList = new ArrayList<>();       // 알고리즘을 거쳐 도출된 '더 이상 묶이지 않는' 최종 주항(PI)들의 전체 목록
    List<PI> answer = new ArrayList<>();       // 최종 최소 논리식에 포함될 정답 주항 리스트
    List<PI> row = new ArrayList<>();          // 테이블 분석(EPI 선정)에서 사용할 가로줄 데이터 (유효한 PI들)
    Map<Integer, List<PI>> column = new HashMap<>(); // 테이블 분석에서 사용할 세로줄 데이터 (10진수 숫자를 키로 하고, 이를 포함하는 PI 리스트를 값으로 함)

    /**
     * [input 함수]
     * 사용자로부터 필요한 데이터를 콘솔에서 입력받아 변수들에 저장하는 초기화 단계입니다.
     */
    @Override
    public void input() {
        Scanner sc = new Scanner(System.in);
        
        // 1. 사용할 변수(비트) 개수 입력
        System.out.print("비트 개수 입력: ");
        bitCount = sc.nextInt();

        // 2. 반드시 결과가 1이어야 하는 minterm 개수와 각 숫자들 입력
        System.out.print("minterm 개수 입력: ");
        int mCount = sc.nextInt();
        System.out.print("minterm들 입력 (공백 구분): ");
        for (int i = 0; i < mCount; i++) {
            minterms.add(sc.nextInt());
        }

        // 3. 0이든 1이든 상관없는 don't care 개수와 각 숫자들 입력 (있는 경우에만 입력 진행)
        System.out.print("don't care 개수 입력: ");
        int dcCount = sc.nextInt();
        if (dcCount > 0) {
            System.out.print("don't care들 입력 (공백 구분): ");
            for (int i = 0; i < dcCount; i++) {
                dontcares.add(sc.nextInt());
            }
        }
    }

    /**
     * [makePI 함수]
     * 입력받은 숫자들을 2진수로 바꾸고, 이웃하는 항끼리 (1비트만 다른 것끼리) 반복적으로 묶어서
     * 더 이상 묶이지 않는 최종적인 주항(Prime Implicants, PI)을 도출하는 핵심 로직입니다.
     */
    @Override
    public void makePI() {
        List<PI> current = new ArrayList<>();
        
        // minterm과 dontcare를 합쳐 모든 항을 다룹니다 (최적화를 위해 dontcare도 묶을 때 같이 사용함)
        List<Integer> allTerms = new ArrayList<>(minterms);
        allTerms.addAll(dontcares);

        // 1. 각 10진수 숫자를 비트 수에 맞추어 2진수 문자열로 변환하여 초기 PI 리스트 생성
        for (int term : allTerms) {
            String b = Integer.toBinaryString(term);
            // 자릿수가 부족하면 앞에 '0'을 채워줍니다 (예: 5비트인데 숫자 3은 '11'이므로 '00011'로 변환)
            while (b.length() < bitCount) {
                b = "0" + b;
            }
            List<Integer> m = new ArrayList<>();
            m.add(term);
            current.add(new PI(b, m));
        }

        // 2. 루프를 돌며 더 이상 묶이지 않을 때까지 결합 연산을 반복합니다.
        while (true) {
            // 현재 리스트를 '1'의 개수에 따라 분류합니다 (0개짜리 그룹, 1개짜리 그룹 등)
            List<List<PI>> groups = grouping(current);
            List<PI> nextLevel = new ArrayList<>(); // 결합을 통해 새로 만들어진 다음 단계의 PI 리스트
            boolean[] combined = new boolean[current.size()]; // 현재 단계에서 어떤 PI가 결합에 쓰였는지 체크하는 배열
            boolean anyCombined = false; // 결합이 단 한 번이라도 일어났는지 여부

            // 1의 개수가 i개인 그룹과 i+1개인 그룹끼리 비교합니다.
            for (int i = 0; i < bitCount; i++) {
                for (PI p1 : groups.get(i)) {
                    for (PI p2 : groups.get(i + 1)) {
                        
                        // 두 PI의 2진수 문자열에서 서로 다른 비트가 몇 개인지, 그리고 위치가 어디인지 체크
                        int diffCount = 0, diffIdx = -1;
                        for (int k = 0; k < bitCount; k++) {
                            if (p1.bit.charAt(k) != p2.bit.charAt(k)) {
                                diffCount++;
                                diffIdx = k;
                            }
                        }

                        // 딱 1비트만 다르다면 두 항을 합칠 수 있습니다!
                        if (diffCount == 1) {
                            anyCombined = true;
                            // 두 항이 다음 결합에 참여했음을 표시 (나중에 안 묶인 항들을 찾기 위함)
                            combined[current.indexOf(p1)] = true;
                            combined[current.indexOf(p2)] = true;

                            // 다른 비트 부분을 와일드카드 문자 '-'로 바꿉니다. (예: "000"과 "001" 결합 -> "00-")
                            StringBuilder sb = new StringBuilder(p1.bit);
                            sb.setCharAt(diffIdx, '-');
                            String nextBit = sb.toString();

                            // 중복 체크: 이미 동일한 비트 패턴이 다음 단계(nextLevel)에 있는지 검사
                            boolean isDuplicate = false;
                            for (PI p : nextLevel) {
                                if (p.bit.equals(nextBit)) {
                                    isDuplicate = true;
                                }
                            }
                            // 중복이 아니라면 새로운 PI를 생성하여 추가
                            if (!isDuplicate) {
                                List<Integer> nextM = new ArrayList<>(p1.minterm);
                                for (int m : p2.minterm) {
                                    if (!nextM.contains(m)) {
                                        nextM.add(m); // 커버하는 minterm 목록도 병합
                                    }
                                }
                                nextLevel.add(new PI(nextBit, nextM));
                            }
                        }
                    }
                }
            }

            // 이번 결합 주기에서 단 한번도 묶이지 않은(결합에 참여하지 않은) 항들은 
            // 가장 단순화된 형태이므로 최종 주항 리스트(piList)에 저장합니다.
            for (int i = 0; i < current.size(); i++) {
                if (!combined[i]) {
                    boolean isDuplicate = false;
                    for (PI p : piList) {
                        if (p.bit.equals(current.get(i).bit)) {
                            isDuplicate = true;
                        }
                    }
                    if (!isDuplicate) {
                        piList.add(current.get(i));
                    }
                }
            }

            // 이번 루프에서 단 한 번도 새로운 결합이 발생하지 않았다면, 더 이상 단순화가 불가능하므로 반복을 멈춥니다.
            if (!anyCombined) {
                break;
            }
            current = nextLevel; // 다음 단계를 현재 단계로 바꾼 뒤 다시 반복
        }
    }

    /**
     * [grouping 함수]
     * 2진수 비트 문자열에서 '1' 문자 개수를 세어, 그룹별로 나누어 담습니다.
     * Quine-McCluskey 알고리즘은 1의 개수 차이가 1개인 인접한 그룹끼리만 묶일 수 있기 때문에 비교 횟수를 줄이는 데 아주 중요합니다.
     */
    @Override
    public List<List<PI>> grouping(List<PI> currentPIs) {
        List<List<PI>> groups = new ArrayList<>();
        // 0개부터 bitCount개까지 1의 개수 그룹 리스트를 만듦
        for (int i = 0; i <= bitCount; i++) {
            groups.add(new ArrayList<>());
        }
        
        // 각 PI를 순회하며 1의 개수를 세고 알맞은 그룹에 삽입
        for (PI pi : currentPIs) {
            int ones = 0;
            for (char c : pi.bit.toCharArray()) {
                if (c == '1') ones++;
            }
            groups.get(ones).add(pi);
        }
        return groups;
    }

    /**
     * [optimize 함수]
     * 도출된 주항(PI)들 중에서, 오직 don't care 숫자로만 채워져 있는 불필요한 항들은 배제합니다.
     * 그리고 남은 핵심 주항들을 가지고 테이블(가로줄 row, 세로줄 column) 분석을 위한 데이터 구조를 초기화합니다.
     */
    @Override
    public void optimize() {
        for (PI pi : piList) {
            boolean hasRealMinterm = false;
            // 해당 PI가 커버하는 minterm 목록에 실제 중요한 minterm(don't care가 아닌 것)이 단 하나라도 포함되어 있는지 검사
            for (int m : pi.minterm) {
                if (!isDontcare(m)) {
                    hasRealMinterm = true;
                }
            }

            // 실제 minterm이 1개라도 겹쳐 있는 유효한 PI만 테이블에 등록
            if (hasRealMinterm) {
                row.add(pi); // 가로줄(후보 PI 목록)에 추가
                
                // 세로줄(각 10진수 minterm별로 자신을 커버해주는 PI 목록) 맵을 구성
                for (int m : pi.minterm) {
                    if (!isDontcare(m)) {
                        if (!column.containsKey(m)) {
                            column.put(m, new ArrayList<>());
                        }
                        column.get(m).add(pi);
                    }
                }
            }
        }
        calculate(); // 준비가 끝났으므로 정답 계산 시작
    }

    /**
     * [calculate 함수]
     * 최종 논리식에 포함시킬 PI들을 고르는 결정적인 과정입니다.
     * 1단계로 반드시 포함되어야만 하는 '필수 주항(EPI)'들을 고르고, 
     * 2단계로 남은 minterm을 덮기 위해 가장 많은 대상을 포함하는 PI를 하나씩 골라 넣는 '탐욕적 방식(Greedy)'을 사용합니다.
     */
    @Override
    public void calculate() {
        // 1. 필수 주항(EPI, Essential Prime Implicant) 찾기
        // 특정 minterm을 커버하는 PI가 단 '1개'뿐이라면, 그 minterm을 회로로 커버하기 위해서 해당 PI는 무조건 정답에 들어가야 합니다.
        for (int m : column.keySet()) {
            if (column.get(m).size() == 1) {
                PI epi = column.get(m).get(0);
                if (!answer.contains(epi)) {
                    answer.add(epi); // 정답(최종식)에 추가
                }
            }
        }

        // 2. 이미 선택된 필수 주항들에 의해 켜지도록 처리가 완료되지 않은 남은 minterm들을 찾습니다.
        List<Integer> remain = new ArrayList<>();
        for (int m : minterms) {
            boolean covered = false;
            for (PI ansPI : answer) {
                if (ansPI.minterm.contains(m)) {
                    covered = true; // 이미 커버되었음
                }
            }
            if (!covered) {
                remain.add(m); // 아직 커버되지 않은 대상을 목록에 추가
            }
        }

        // 3. 탐욕(Greedy) 방식의 나머지 커버링 진행
        // 아직 남아 있는 minterm이 존재한다면, 남은 minterm들을 "가장 많이 동시에 덮어줄 수 있는" 최선의 PI를 찾아 정답에 추가하는 일을 반복합니다.
        while (!remain.isEmpty()) {
            PI bestPi = null;
            int maxCover = 0;

            for (PI p : row) {
                if (answer.contains(p)) continue; // 이미 정답에 포함된 것은 건너뜀
                
                // 해당 PI가 현재 남아있는 minterm들 중 몇 개를 커버하는지 카운트
                int coverCount = 0;
                for (int m : p.minterm) {
                    if (remain.contains(m)) {
                        coverCount++;
                    }
                }
                
                // 더 많은 개수를 커버하는 PI를 업데이트
                if (coverCount > maxCover) {
                    maxCover = coverCount;
                    bestPi = p;
                }
            }
            
            // 더 이상 남은 minterm들을 덮을 수 있는 PI가 없다면 반복 종료
            if (bestPi == null) {
                break;
            }
            
            answer.add(bestPi); // 찾은 최선의 PI를 정답 리스트에 추가
            
            // 이 PI가 덮어준 minterm들을 남은 리스트(remain)에서 지워줍니다.
            for (int m : bestPi.minterm) {
                remain.remove(Integer.valueOf(m));
            }
        }
    }

    /**
     * [isDontcare 함수]
     * 입력받은 숫자가 don't care 목록에 들어 있는지 판단하여 true / false로 돌려줍니다.
     */
    @Override
    public boolean isDontcare(int minterm) {
        return dontcares.contains(minterm);
    }

    /**
     * [parse 함수]
     * 내부적으로 쓰이는 2진수 표현(예: "01--")을 논리 설계식 형태(예: "x1'x2")로 바꿉니다.
     * - '1'이면 원래의 변수(x1, x2 등)로 표현하고,
     * - '0'이면 뒤에 프라임(')을 붙여 부정을 표현하며,
     * - '-'는 상관없는 조건이므로 식에서 제외시킵니다.
     */
    @Override
    public String parse(PI pi) {
        String result = "";
        for (int i = 0; i < pi.bit.length(); i++) {
            char c = pi.bit.charAt(i);
            if (c == '1') {
                result += "x" + (i + 1);
            } else if (c == '0') {
                result += "x" + (i + 1) + "'";
            }
            // '-'인 경우는 아무 문자도 추가하지 않고 넘어감
        }
        return result;
    }

    /**
     * [print 함수]
     * 정답 리스트(answer)에 들어있는 식들을 각각 문자로 파싱(parse)한 후,
     * OR 결합을 의미하는 "+" 기호를 사용하여 하나로 합쳐서 콘솔에 예쁘게 출력합니다.
     */
    @Override
    public void print() {
        String finalSOP = "";
        for (int i = 0; i < answer.size(); i++) {
            finalSOP += parse(answer.get(i));
            if (i < answer.size() - 1) {
                finalSOP += " + "; // 항 사이사이에만 + 기호 넣기
            }
        }
        System.out.println("최종 최소 논리식: " + finalSOP);
    }

    /**
     * 프로그램 실행 진입점 (main 함수)
     * 객체를 생성하고, 규격(인터페이스)에 따라 input -> makePI -> optimize -> print 순서대로 실행시킵니다.
     */
    public static void main(String[] args) {
        LogicOptimizer optimizer = new LogicOptimizer();
        optimizer.input();
        optimizer.makePI();
        optimizer.optimize();
        optimizer.print();
    }
}
