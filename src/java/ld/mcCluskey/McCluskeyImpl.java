import java.util.*;


public class McCluskeyImpl implements McCluskey{
    private final Scanner sc;

    private int bit;
    private ArrayList<Integer> minterms;
    private ArrayList<Integer> dontcare;

    public McCluskeyImpl(Scanner sc) {
        this.sc = sc;
    }

    @Override
    public void input(){
        
        do { 
            bit = Integer.parseInt(sc.nextLine());
        } while (bit <= 0);

        int mintermsNumber;
        int dontcareNumber;
        do { 
            mintermsNumber = Integer.parseInt(sc.nextLine());
            dontcareNumber = Integer.parseInt(sc.nextLine());
        } while (mintermsNumber < 0 || dontcareNumber < 0);
        
        minterms = new ArrayList<>();
        dontcare = new ArrayList<>();
        
        for (int i = 0; i < mintermsNumber; i++) {
            int number = Integer.parseInt(sc.nextLine());
            minterms.add(number);
        }

        for (int i = 0; i < dontcareNumber; i++) {
            int number = Integer.parseInt(sc.nextLine());
            dontcare.add(number);
        }
        
    }
    @Override
    public void makePI(ArrayList<Integer> minterms){
        // 첫번째 작업: PI 만들기

        // 1.	애들을 다 받아서 넣고
        ArrayList<Integer> PI;

        // 2.	1의 갯수로 그룹핑
        grouping();
        // 3.	1비트씩 다른 애들끼리 다 묶기

        // 4.	계속 묶으면서 안묶이는 애들은 PI 리스트에 넣기
        // 5.	그리고 안묶이면 반복문 탈출
    }
    @Override
    public void grouping(ArrayList<Integer> binaryArrayList){
        ArrayList<ArrayList<Integer>> groups = new ArrayList<>();
        for (int i = 0; i <= bit; i++) {
            groups.add(new ArrayList<>());
        }

        for (int i = 0; i < binaryArrayList.size(); i++) {
            int ones = Integer.bitCount(binaryArrayList.get(i));
            groups.get(ones).add(binaryArrayList.get(i));
        }
    }

    @Override
    public void optimize(){

    }
    @Override
    public void calculate(){
    // 두번째 작업: Answer 구하기
        
    }
    @Override
    public void print(){
        
    }
    @Override
    public void parse(){
        
    }
    @Override
    public void isDontcare(){
        // 중간 작업: Don’t’care 제거하기
    }

    
    public static void main(String[] args) {
            Scanner sc = new Scanner(System.in);
            McCluskeyImpl mcCluskeyImpl = new McCluskeyImpl(sc);
            mcCluskeyImpl.input();
            
            
    }
}


// class PI{
//     private 
//     public 
// }