# QuineMcCluskey.java - 알고리즘 논리 구조

---

## 전체 함수 호출 흐름

```
main()
  │
  ├─ [입력] bits, minterms, dontCares
  │
  └─ solve(bits, minterms, dontCares)
       │
       ├─ [PI 생성 루프]
       │    ├─ toBinaryString(term, bits)       ← 각 항 binary 변환
       │    ├─ imp.getOneCount()                ← 1의 개수로 그룹핑
       │    ├─ imp1.combine(imp2)               ← 인접 그룹 쌍 결합
       │    └─ imp.checked == false → primeImplicants 수집
       │
       ├─ [Don't Care 제거]
       │    └─ pi.minterms ∩ minterms == ∅ → piList에서 제외
       │
       ├─ [Chart Reduction 루프] (changed == true이면 반복)
       │    │
       │    ├─ ① EPI 찾기
       │    │    ├─ cols[] 순회 → coverCount == 1인 minterm 탐색
       │    │    ├─ epiList.add(epi)
       │    │    ├─ cols[i] = -1   (커버된 minterm 제거)
       │    │    └─ rows[idx] = null (해당 PI 제거)
       │    │
       │    ├─ ② 세로비교 (Column Dominance)
       │    │    ├─ getCoveringPIIndices(m1, rows) → piIndices1
       │    │    ├─ getCoveringPIIndices(m2, rows) → piIndices2
       │    │    └─ piIndices1.containsAll(piIndices2) → cols[i] = -1
       │    │
       │    └─ ③ 가로비교 (Row Dominance)
       │         ├─ getActiveMintermsCovered(pi1, cols) → activeMinterms1
       │         ├─ getActiveMintermsCovered(pi2, cols) → activeMinterms2
       │         └─ activeMinterms1.containsAll(activeMinterms2) → rows[j] = null
       │
       ├─ [Cyclic Core 처리]
       │    └─ solveCyclicCore(remainingCols, remainingPIs)
       │         └─ findBestCoverBacktrack(minterms, pis, index, current, bestCover)
       │               └─ coversAll(current, minterms)  ← 종료 조건 확인
       │
       └─ [출력]
            └─ parsePI(mask, bits)  ← mask → 변수 표현식 변환
```

---

## 함수별 역할

### `main()`
- `Scanner`로 `bits`, `mintermCount`, `minterms`, `dcCount`, `dontCares` 순으로 입력받음
- `solve()` 호출

---

### `solve(bits, minterms, dontCares)`
전체 알고리즘 진행 담당. 내부 동작:

**① PI 생성**
- `minterms + dontCares` → `allTerms`로 합침
- 각 항을 `toBinaryString(term, bits)`로 변환 후 `Implicant` 생성 → `currentLevel`
- 루프:
  - `imp.getOneCount()`로 그룹(`groups[0..bits]`) 분류
  - 인접 그룹(`groups[i]` ↔ `groups[i+1]`) 에서 `imp1.combine(imp2)` 시도
  - 성공 시 `nextLevel`에 추가, `checked = true`
  - `currentLevel`에서 `checked == false`인 항 → `primeImplicants`에 추가
  - 결합 없으면 루프 종료

**② Don't Care 제거**
- `primeImplicants` 중 `pi.minterms`가 실제 `minterms`를 하나도 포함하지 않는 PI 제거 → `piList` 구성

**③ Chart Reduction (changed 루프)**
- `cols = List<Integer>` (minterms 복사, 제거 시 `-1`)
- `rows = List<Implicant>` (piList 복사, 제거 시 `null`)
- 순서: EPI → 세로비교 → 가로비교 (변화 발생 시 처음으로 돌아감)

**④ Cyclic Core**
- `cols`에 `-1`이 아닌 항이 남아있으면 `solveCyclicCore()` 호출

**⑤ 출력**
- `finalAnswer = epiList + otherSelectedPIs`
- 각 PI에 대해 `parsePI(mask, bits)` 호출해서 표현식 조합

---

### `Implicant.getOneCount()`
- `mask`에서 `'1'` 문자 개수 세어 반환
- 그룹 분류에 사용

---

### `Implicant.combine(Implicant other)`
- 두 항의 `mask`를 비교, 다른 비트 수(`diffCount`) 계산
- `diffCount == 1`이면:
  - 다른 위치(`diffIdx`)를 `'-'`로 교체한 새 `Implicant` 반환
  - `this.checked = true`, `other.checked = true`
- 그 외엔 `null` 반환

---

### `toBinaryString(int val, int bits)`
- `Integer.toBinaryString(val)` 결과를 `bits` 자리수로 zero-padding
- 예: `val=5, bits=4` → `"0101"`

---

### `getCoveringPIIndices(int minterm, List<Implicant> rows)`
- `rows`를 순회하며 `pi.minterms.contains(minterm)` 인 PI의 **인덱스** Set 반환
- **세로비교**에서 두 minterm의 커버 PI 집합 비교에 사용

---

### `getActiveMintermsCovered(Implicant pi, List<Integer> cols)`
- `pi.minterms` 중 `cols`에 현재 살아있는(`!= -1`) minterm만 모아 반환
- **가로비교**에서 두 PI의 커버 minterm 집합 비교에 사용

---

### `getActualMintermsCovered(Implicant pi, List<Integer> actualMinterms)`
- `pi.minterms` 중 실제 `minterms`(Don't Care 미포함)에 속하는 것만 반환
- PI 목록 출력 시 사용

---

### `solveCyclicCore(List<Integer> minterms, List<Implicant> pis)`
- `bestCover`를 초기값 `pis` 전체로 설정 (worst case)
- `findBestCoverBacktrack()` 호출 후 최소 커버 반환

---

### `findBestCoverBacktrack(minterms, pis, index, current, bestCover)`
DFS 백트래킹으로 최소 PI 집합 탐색:

```
종료 조건:
  1. coversAll(current, minterms) == true
     → current.size() < bestCover.size() 이면 bestCover 갱신
  2. current.size() >= bestCover.size() - 1
     → 이미 최적 초과, 가지치기
  3. index == pis.size()
     → 더 선택할 PI 없음

재귀:
  pis[index] 포함 → findBestCoverBacktrack(..., index+1, ...)
  pis[index] 제외 → findBestCoverBacktrack(..., index+1, ...)
```

---

### `coversAll(List<Implicant> current, List<Integer> minterms)`
- `current`의 모든 PI가 가진 `minterms` 합집합이 목표 `minterms`를 포함하면 `true`
- `findBestCoverBacktrack()`의 성공 조건 판단에 사용

---

### `parsePI(String mask, int bits)`
- `mask`의 각 비트를 변수 이름(`A`, `B`, `C`, ...)으로 변환

```
'-' → 해당 비트 무시 (skip)
'1' → 변수 그대로 (예: 'A')
'0' → 변수에 ' 붙임 (예: 'A'')
전부 '-' → "1" 반환
```

---

## 핵심 변수 요약

| 변수 | 타입 | 의미 |
|------|------|------|
| `currentLevel` | `List<Implicant>` | 현재 결합 단계의 항 목록 |
| `primeImplicants` | `Set<Implicant>` | 최종 PI 후보 (checked==false인 항) |
| `piList` | `List<Implicant>` | Don't Care 전용 PI 제거 후 실제 PI 목록 |
| `cols` | `List<Integer>` | 활성 Minterm 목록 (`-1` = 제거됨) |
| `rows` | `List<Implicant>` | 활성 PI 목록 (`null` = 제거됨) |
| `epiList` | `List<Implicant>` | EPI 목록 (최종 답의 앞부분) |
| `otherSelectedPIs` | `List<Implicant>` | Cyclic Core에서 선택된 PI들 |
| `finalAnswer` | `List<Implicant>` | `epiList + otherSelectedPIs` |

---

## 실행 방법

### 1단계: 컴파일

```bash
cd "/Users/cw/Documents/프로젝트/workspaces/google AI -antigravity/logic-design"
javac QuineMcCluskey.java
```

### 2단계: 실행

**직접 입력 방식**
```bash
java QuineMcCluskey
```
`main()`이 `Scanner`로 다음 순서로 입력받음:
```
비트 개수 입력 (e.g. 4): 4
minterm 개수 입력: 9
minterm들 입력 (공백으로 구분): 
0 2 5 6 7 8 10 13 15
don't care 개수 입력: 1
don't care들 입력 (공백으로 구분): 
14
```

**파일 redirect 방식** (test_input.txt 이미 준비됨)
```bash
java QuineMcCluskey < test_input.txt
```

### test_input.txt 형식
```
[비트 수]
[minterm 개수]
[minterm 값들 공백 구분]
[don't care 개수]
[don't care 값들 공백 구분]  ← 개수가 0이면 이 줄 불필요
```

### 예상 출력 (test_input.txt 기준)
```
Found Prime Implicants (PI):
PI 0: -0-0 covering actual minterms: [0, 2, 8, 10]
PI 1: --10 covering actual minterms: [2, 6, 10]
PI 2: -1-1 covering actual minterms: [5, 7, 13, 15]
PI 3: -11- covering actual minterms: [6, 7, 15]

Found EPI: -0-0 (covers minterm 0)
Found EPI: -1-1 (covers minterm 5)
Row Dominance: PI --10 dominates -11-. Removing PI -11-
Found EPI: --10 (covers minterm 6)

--- Final Result ---
Minimized Formula: B'D' + BD + CD'
```

### Java가 없는 경우 설치
```bash
# Homebrew (macOS)
brew install openjdk@17
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
```
