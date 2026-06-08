// [데이터구조 설계]
// 제안서 3-1: Prime Implicant 하나를 표현하기 위한 클래스
import java.util.ArrayList;
import java.util.List;

public class PI {
    String bit;             // PI의 비트 형태 (예: "10-1")
    List<Integer> minterm;  // 해당 PI가 포함하는 minterm 번호들
    int count;              // 포함하는 minterm 개수
    boolean used;           // 다른 PI와 묶였는지 확인 (묶였으면 true)
    boolean removed;        // 가로 비교 등에서 제거되었는지 확인 (제거되었으면 true)

    public PI(String bit, List<Integer> minterm) {
        this.bit = bit;
        this.minterm = new ArrayList<>(minterm);
        this.count = minterm.size();
        this.used = false;
        this.removed = false;
    }
}