# McCluskeyImpl.java - 알고리즘 논리 구조

---

## 파일 구성

| 파일 | 역할 |
|------|------|
| `Main.java` | 진입점. Scanner 생성 후 `solve()` 호출 |
| `McCluskey.java` | 인터페이스 (input/makePI/grouping/optimize/calculate/parse/print/solve) |
| `McCluskeyImpl.java` | 알고리즘 구현체 |
| `PI.java` | Prime Implicant 하나를 표현하는 자료구조 |

---

## 전체 함수 호출 흐름

```
Main.main()
  │
  └─ McCluskeyImpl.solve()
       │
       ├─ input()        ← bits, minterms, dontcares 입력
       │
       ├─ makePI()       ← Prime Implicant 생성
       │    ├─ initializePI(allTerms)      ← 각 항을 binary 문자열 PI로 변환
       │    ├─ grouping(currentPI)          ← '1'의 개수로 그룹핑
       │    ├─ canMerge(a, b)               ← 결합 가능 여부 판단
       │    ├─ merge(a, b)                  ← 인접 그룹 쌍 결합
       │    ├─ isDuplicate(newPI, merged)   ← 중복 PI 제외
       │    └─ pi.used == false → primeImplicants 수집
       │
       ├─ optimize()     ← Don't Care로만 이루어진 PI 제거
       │
       ├─ calculate()    ← 최종 답(answer) 도출
       │    ├─ buildChart()         ← rows / columns 차트 구성
       │    ├─ simplify()           ← (변화 없을 때까지 반복)
       │    │     ├─ findEPI()        ← ① EPI 추출
       │    │     ├─ removeRows()     ← ② 가로비교 (Row Dominance)
       │    │     └─ removeColumns()  ← ③ 세로비교 (Column Dominance)
       │    └─ coverRemaining()     ← 남은(cyclic) minterm을 greedy로 커버
       │
       └─ print()        ← parse()로 식 변환 후 출력
            └─ parse(answer)  ← bit 패턴 → x1x2' 표현식 변환
```

---

## 함수별 역할

### `main()`
- `Scanner`로 입력 스트림을 만들고 `McCluskeyImpl` 생성 → `solve()` 호출

---

### `solve()`
전체 알고리즘 실행 순서만 제어:
`input()` → `makePI()` → `optimize()` → `calculate()` → `print()`

---

### `input()`
과제 입력 포맷 순서대로 읽음 (Scanner는 공백·개행을 무시하므로 토큰 순서만 중요):

1. 변수(비트) 개수
2. minterm 개수
3. don't care 개수
4. minterm 목록
5. don't care 목록

---

### `makePI()`
Prime Implicant 생성:

- `minterms + dontcares` → `initialTerms`로 합침
- `initializePI()`로 각 항을 비트 문자열 PI로 변환 → `currentPI`
- 루프:
  - `grouping()`으로 `'1'`의 개수별 그룹(`groups[0..bits]`) 분류
  - 인접 그룹(`groups[i]` ↔ `groups[i+1]`)에서 `canMerge()` 통과 쌍을 `merge()`
  - `merge` 결과를 `isDuplicate()`로 확인 후 `newPI`에 추가
  - 결합에 쓰인 두 PI는 `used = true` 표시
  - `currentPI` 중 `used == false`인 항(= 더 못 합쳐지는 PI) → `primeImplicants`에 수집
  - 이번 라운드에 병합이 한 번도 없으면 루프 종료

---

### `initializePI(terms)`
- 각 정수를 `bits` 자리 이진 문자열로 변환(`String.format` zero-padding)하여 PI 생성
- 예: `term=5, bits=4` → `"0101"`

---

### `grouping(currentPIs)`
- 각 PI의 `bit`에서 `'1'` 문자 개수를 세어 `groups[oneCount]`에 분류
- 인접 그룹끼리만 비교하면 되므로 비교량을 줄임

---

### `canMerge(a, b)`
- 두 PI의 `bit`를 자리별로 비교
- `'-'`(don't care 비트) **위치가 서로 다르면 결합 불가** (`return false`)
- 실제 비트가 정확히 **1자리만 다르면** 결합 가능 (`return different == 1`)

---

### `merge(a, b)`
- 같은 자리는 그대로, 다른 한 자리는 `'-'`로 교체 → 새 `bit`
- 두 PI의 `minterm`을 합집합으로 묶어 새 `PI` 반환

---

### `isDuplicate(newPI, mergedPI)`
- 같은 `bit` 패턴의 PI가 이미 `newPI`에 있으면 `true` → 중복 추가 방지

---

### `optimize()`
- `primeImplicants` 중 **커버하는 항이 전부 don't care인 PI 제거**
- 실제 출력 1을 만드는 데 기여하지 않는 PI를 답 후보에서 뺌

---

### `calculate()`
- `buildChart()` → `simplify()` → `coverRemaining()` 순으로 최종 답 도출 (흐름만 제어)

---

### `buildChart()`
- `rows` = `primeImplicants` 복사 (후보 PI = 행)
- `columns` = 각 minterm을 덮는 PI 목록 (minterm = 열)

---

### `simplify()`
- `prev = rows.size + columns.size` 저장
- `findEPI()` → `removeRows()` → `removeColumns()` 1회 수행
- 크기가 더 줄지 않으면(`prev`와 같으면) 종료하는 `do-while` 루프
- → EPI/dominance를 변화가 없을 때까지 반복 적용

---

### `findEPI()`
- `columns` 중 **크기가 1인 열**(= 단 하나의 PI만 덮는 minterm) 탐색
- 해당 PI를 `answer`에 추가, `rows`에서 제거
- 그 PI가 덮는 minterm 열을 모두 제거
- 차트가 바뀌었으므로 처음부터 다시 검사

---

### `removeRows()` — 가로비교 (Row Dominance)
- 행 `A`가 행 `B`의 `minterm`을 모두 포함(`A ⊇ B`)하면 `B`는 불필요 → 제거
- 제거는 일단 `null`로 표시한 뒤, 마지막에 한꺼번에 걷어냄

---

### `removeColumns()` — 세로비교 (Column Dominance)
- 열 `i`가 열 `j`를 덮는 PI를 모두 포함(`i ⊇ j`)하면 `i`는 불필요 → 제거
- (`j`가 덮이면 `i`도 자동으로 덮이므로)

---

### `coverRemaining()`
EPI/dominance로도 안 풀린 **cyclic core**를 greedy로 마무리:

- `remaining` = `minterms` 중 `answer`가 아직 안 덮은 항
- `remaining`이 빌 때까지 반복:
  - `rows` 중 `remaining`을 **가장 많이 덮는 PI**를 선택
  - 그 PI를 `answer`에 추가하고, 덮은 minterm을 `remaining`에서 제거
  - 더 덮을 게 없으면 종료

---

### `parse()`
`answer`의 각 PI `bit`를 변수 표현식으로 변환:

```
'-' → 해당 비트 무시 (skip)
'1' → 변수 그대로     (위치 j → x(j+1))
'0' → 변수에 ' 붙임   (위치 j → x(j+1)')
전부 '-' (식이 빔)   → "1" 반환
```

- PI 사이는 `" + "`로 연결
- 예: `"10-1"` → `x1 x2' x4` → `"x1x2'x4"`

---

### `print()`
- `parse()` 결과 문자열을 출력 (최종 최소화 식만 출력)

---

## 핵심 변수 요약

| 변수 | 타입 | 의미 |
|------|------|------|
| `bits` | `int` | 변수(비트) 개수 |
| `minterms` | `List<Integer>` | 출력이 1인 항 |
| `dontcares` | `List<Integer>` | don't care 항 |
| `primeImplicants` | `List<PI>` | 생성된 Prime Implicant (`used==false`인 항) |
| `answer` | `List<PI>` | 최종 정답 PI (EPI + greedy 선택분) |
| `rows` | `List<PI>` | 활성 PI 목록 (행), `null` = 제거됨 |
| `columns` | `List<List<PI>>` | 각 minterm을 덮는 PI 목록 (열) |

### `PI` 클래스

| 필드 | 타입 | 의미 |
|------|------|------|
| `bit` | `String` | PI 비트 패턴 (예: `"10-1"`) |
| `minterm` | `List<Integer>` | 이 PI가 덮는 minterm 번호들 |
| `used` | `boolean` | 다른 PI와 결합됐는지 (`true`면 PI 후보에서 제외) |

---

## 실행 방법

### 1단계: 컴파일

```bash
javac Main.java McCluskey.java McCluskeyImpl.java PI.java
```
(또는 디렉터리에서 `javac *.java`)

### 2단계: 실행

**직접 입력 방식**
```bash
java Main
```
입력 순서 (과제 포맷):
```
[변수 개수]
[minterm 개수] [don't care 개수]
[minterm 값들 공백 구분]
[don't care 값들 공백 구분]   ← 개수가 0이면 이 줄 생략
```

**파일 redirect 방식**
```bash
java Main < ex2.txt
```

### 예제 (Example2 기준)

입력 (`ex2.txt`):
```
5
8 4
1 3 10 14 21 26 28 30
5 12 17 29
```

출력:
```
x2x4x5' + x1'x2'x3'x5 + x1x3x4'x5 + x1x2x3x4'
```

> 프로그램은 중간 과정(PI 목록·EPI 로그 등)을 출력하지 않고 **최종 최소화 식 한 줄만** 출력한다.

### Java가 없는 경우 (macOS)

```bash
brew install openjdk@17
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
```
