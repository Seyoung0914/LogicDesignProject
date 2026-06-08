import java.util.*;

public class Input {

    Scanner sc;

    int bitCount;               // 비트 개수
    int mintermNum;             // minterm 개수
    int dontCareNum;            // don't care 개수
    ArrayList<Integer> mintermList  = new ArrayList<>();
    ArrayList<Integer> dontCareList = new ArrayList<>();

    public Input(Scanner sc) {
        this.sc = sc;
    }

    public void input() {

        // 비트 개수 입력
        System.out.print("비트 개수: ");
        bitCount = sc.nextInt();

        // minterm 개수 입력
        System.out.print("minterm 개수: ");
        mintermNum = sc.nextInt();

        // minterm 값 하나씩 입력
        for (int i = 0; i < mintermNum; i++) {
            System.out.print((i + 1) + "번째 minterm: ");
            mintermList.add(sc.nextInt());
        }

        // don't care 개수 입력
        System.out.print("don't care 개수: ");
        dontCareNum = sc.nextInt();

        // don't care 값 하나씩 입력
        for (int i = 0; i < dontCareNum; i++) {
            System.out.print((i + 1) + "번째 don't care: ");
            dontCareList.add(sc.nextInt());
        }
    }
}
