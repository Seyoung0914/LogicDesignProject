import java.util.*;

public class InputHandler {

    Scanner sc;

    int bCount;               // 비트 개수
    int mNum;             // minterm 개수
    int dcNum;            // don't care 개수
    ArrayList<Integer> mintermList  = new ArrayList<>();
    ArrayList<Integer> dontCareList = new ArrayList<>();

    public InputHandler(Scanner sc) {
        this.sc = sc;
    }

    public void input() {

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
}
