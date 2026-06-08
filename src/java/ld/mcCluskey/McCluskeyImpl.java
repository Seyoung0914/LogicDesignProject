import java.util.ArrayList;
import java.util.List;

public class McCluskeyImpl implements McCluskey {

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
        /*
         * [입력받기]
         *
         * 1. 비트 개수를 입력받는다.
         *    예: 4비트이면 minterm을 0000 ~ 1111 형태로 표현한다.
         *
         * 2. minterm들을 입력받는다.
         *    실제 출력 결과가 1이 되는 항들이다.
         *
         * 3. don't care 항들을 입력받는다.
         *    결과가 0이어도 되고 1이어도 되는 항들이다.
         *    PI를 만들 때는 사용하지만, 최종 answer에는 단독으로 남기면 안 된다.
         */
    }

    @Override
    public void makePI() {
        /*
         * [첫번째 작업: PI 만들기]
         *
         * 1. minterm과 don't care를 모두 PI 후보로 만든다.
         * 2. 각 PI를 1의 개수 기준으로 그룹핑한다.
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
        return List.of();
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
         *
         * 2. row에는 PI들을 저장한다.
         *    의미: 각 PI가 어떤 minterm들을 커버하는지 저장한다.
         *
         * 3. column에는 minterm들을 저장한다.
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