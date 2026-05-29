import java.util.ArrayList;

interface McCluskey{

    // 입력 받기
    void input();
    // PI 만들기
    void makePI(ArrayList<Integer> minterms);
    // 1의 갯수로 그룹핑하기
    void grouping(ArrayList<Integer> binaryArrayList);
    // 최적화하기
    void optimize();
    // 계산하기
    void calculate();
    // 룰력하기
    void print();
    // 파싱하기
    void parse();
    // 돈케어인지 확인하기
    void isDontcare();
}