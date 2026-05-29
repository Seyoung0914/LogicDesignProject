import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        McCluskeyImpl mcCluskeyImpl = new McCluskeyImpl(sc);
        mcCluskeyImpl.input();
        
    }
}
    
   

    
    

    


    // Row
    // Column => class class배열로 하면 되지 않냐
    // // class => PI count , 해당하는 PI[], minterm번호 -> class를 만들까?
    // // ROW, COLUMN TABLE[row][column] -> 은 minterm  = {p1, p2}COLUMN
    
    // 1.	Answer 배열, row 배열, column 배열 (이름은 차후 선정)
    // A.	Answer배열은 비워놓는다
    // B.	row배열은 PI들 다 넣어놓고
    // C.	column배열 Minterm들에 해당하는 PI들을 다 넣어놓는다.
    // 2차원 배열을 사용한다. Column[Minterm][PI]
    // 2.	EPI 찾기
    // A.	Miterm 배열을 순회하면서 사이즈가 1이면 EPI배열에 해당 PI를 넣으면 됌
    // 3.	세로비교
    // A.	2중 For문을 돌린다
    // i.	바깥 For
    // 1.	Minterm 하나를 정한다 -> A
    // ii.	안쪽 For
    // 1.	A == B 인 애는 제외해야한다.
    // 2.	안쪽For문에서 정한 if(A.contains(B)) 연산해서 비교한다.
    // 3.	If(A.contains(B)) true라면, A를 제거한다. 그렇지 않으면 다음 B값으로 넘어간다.
    // A.	어떻게 지우노? -> -1로 minterm을 바꾼다.
    // 4.	가로비교
    // A.	2중 For문돌린다.
    // i.	바깥 For
    // 1.	PI 하나를 정한다 -> A
    // ii.	안쪽 For
    // 1.	PI 하나를 정한다 -> B
    // 2.	A == B 인 애는 제외해야 한다.
    // 3.	안쪽For문에서 정한 if(A.contains(B)) 연산해서 비교한다.
    // 4.	If(A.contains(B)) true라면, B를 제거한다. 그렇지 않으면 다음 B값으로 넘어간다.
    // A.	어떻게 지우노? -> NULL로 minterm을 바꾼다.

    // 세번째 작업: 출력하기
    // Answer문을 돌면서 출력한다 -> X
    // 1.	Parse() 함수를 쓸건데
    // 2.	2중반복문을 돌린다.
    // A.	EPI가 Don’t care 혹은 PI가 모두 don’t care로 구성되어있는경우 그냥 다 무시한다.
    // B.	PI가 생긴 꼴은 ‘-‘, ‘0’, ‘1’ 로 구성되어있다.
    // i.	if(‘-‘) 이면 그 비트룰 무시한다
    // ii.	else if(
    // 3.	If(bit가 0이면) ‘를 붙인다 1이면 그냥 ㄱㄱ

    // Answer -> EPI를 먼저 넣고, 마지막에 EPI에 포함 안되는 PI를 넣는다.
    // PI들을 어떻게 넣어놓냐인데
    // PI들을 민텀번호들을 넣어놓을까
    // 최적화 PI들을 이용해서
    // 1.	EPI를 찾는다
    // 2.	세로비교
    // i.	Minterm들에 대해서 포함하는 PI들을 가지는 배열을 만든다.
    // 3.	가로
    // i.	2중 For문 돌리면서 Contains 사용해서 이번에는 A가 C를 포함하면 A가 더 큰건데 여기서는 C버린다. 즉, 더 작은걸 버린다.
    // 4.	EPI를 다 찾으면 남는부분을 커버하는 PI를 찾는다
    // 5.	그걸 정답에 넣는다

    // 정답. (p2, p3)
    // P2,p3 -> 우리가 원하는 형태로 식형태로
    // 그다음에 그걸 출력하면 끝.

