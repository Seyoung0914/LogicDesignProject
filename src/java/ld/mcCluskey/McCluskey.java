import java.util.*;

public interface McCluskey {
        void input();                    // 사용자에게 값을 입력받는 함수 (4-1)
        void makePI();                   // Prime Implicant를 생성하는 함수 (4-2)
        List<List<PI>> grouping(List<PI> currentPIs); // 1의 개수에 따라 나누는 함수 (4-3)
        void optimize();                 // 불필요한 PI 제거 및 배열 구성 함수 (4-4)
        void calculate();                // EPI, 세로/가로 비교를 통해 최종 Answer 도출 (4-5)
        boolean isDontcare(int minterm); // 해당 번호가 don't care인지 확인하는 함수 (4-9)
        String parse(PI pi);             // 최종 PI를 문자식(ex: x1x2')으로 변환하는 함수 (4-10)
        void print();                    // 변환된 식들을 +로 연결하여 출력하는 함수 (4-11)

}
