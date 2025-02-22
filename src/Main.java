import java.util.*;
import java.util.stream.Collectors;

class Main {

  private static List<List<List<Integer>>> results = new ArrayList<>();

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    List<Integer> handCards = Arrays.asList(
      23, 24, 25, 16, 17, 18, 13, 14, 15, 12, 22, 11, 21, 24
      // 17, 18, 21, 22, 23, 24, 24, 25
    );
    handCards.sort(Integer::compare);
    System.out.println("sorted handCards: " + handCards);
    // in = (int) handCards.stream().filter(c -> c.equals(51)).count();
    // match(null, handCards, 0, handCards.size());
    match(null, null, handCards, 0, handCards.size());
    assert !results.isEmpty() : "no matched result";
    results.sort(new Comparator<List<List<Integer>>>() {
      @Override
      public int compare(List<List<Integer>> o1, List<List<Integer>> o2) {
        long o1Count = o1.stream().filter(tc -> tc.size() == 1).count();
        long o2Count = o2.stream().filter(tc -> tc.size() == 1).count();
          return Long.compare(o1Count, o2Count);
      }
    });
    System.out.println("final result: " + results);
    List<List<Integer>> bestRes = results.get(0);
    System.out.println("best result: " + bestRes);
    List<Integer> shouldOut = bestRes.stream().filter(tc -> tc.size() == 1).findFirst().orElse(null);
    if (Objects.nonNull(shouldOut)) { // 存在散牌
      // 散牌中有连续的牌则不优先打
      List<Integer> straight = bestRes.stream().filter(tc -> tc.size() == 1).map(tc -> tc.get(0)).collect(Collectors.toList());
      if (straight.size() == 1) {
        System.out.println("should out: " + straight.get(0));
        long end = System.currentTimeMillis();
        System.out.println("cost: " + (end - start) + "ms");
        return;
      } else {
        straight.sort(Integer::compare);
        System.out.println("own straight: " + straight);
        for (int i = 0; i < straight.size(); i += 1) {
          if (i != straight.size() - 1 && straight.get(i) + 1 == straight.get(i + 1)) {
            continue;
          } else if (i != 0 && straight.get(i) - 1 == straight.get(i - 1)) {
            continue;
          } else {
            System.out.println("should out: " + straight.get(i));
            long end = System.currentTimeMillis();
            System.out.println("cost: " + (end - start) + "ms");
            return;
          }
        }
        System.out.println("should out: " + straight.get(0)); // 否则随便打一张
        long end = System.currentTimeMillis();
        System.out.println("cost: " + (end - start) + "ms");
      }
    } else { // 不存在散牌，有取舍地打
      // 拆未完成的顺子
      List<Integer> uncompletedStraight = bestRes.stream().filter(tc -> tc.size() == 2).filter(tc -> tc.get(0) + 1 != tc.get(1)).findFirst().orElse(null);
      // 优先拆普通顺子
      List<Integer> straight = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) + 1 == tc.get(1) && tc.get(0) % 10 != 1).findFirst().orElse(null);
      // 次优拆对子
      List<Integer> pair = bestRes.stream().filter(tc -> tc.size() == 2).findFirst().orElse(null);
      // 实在没有拆二带一
      List<Integer> twoWithOne = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) + 10 == tc.get(1) || tc.get(1) + 10 == tc.get(2)).findFirst().orElse(null);
      // 再没有就拆二七十和一二三
      List<Integer> twoWith70 = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) % 10 == 1 && tc.get(1) % 10 == 6 && tc.get(2) % 10 == 9).findFirst().orElse(null);
      List<Integer> threeWith123 = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) % 10 == 1 && tc.get(1) % 10 == 2 && tc.get(2) % 10 == 3).findFirst().orElse(null);
      if (Objects.nonNull(uncompletedStraight)) {
        System.out.println("should out: " + uncompletedStraight.get(0)); 
      } else if (Objects.nonNull(straight)) {
        System.out.println("should out: " + straight.get(0)); 
      } else if (Objects.nonNull(pair)) {
        System.out.println("should out: " + pair.get(0)); 
      } else if (Objects.nonNull(twoWithOne)) {
        System.out.println("should out: " + twoWithOne.get(0)); 
      } else if (Objects.nonNull(twoWith70)) {
        System.out.println("should out: " + twoWith70.get(0)); 
      } else if (Objects.nonNull(threeWith123)) {
        System.out.println("should out: " + threeWith123.get(2)); // 打最大 
      } else {
        System.out.println("there are no card to out");  
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("cost: " + (end - start) + "ms");
  }

  /**
   * 牌型匹配
   * @param tempRes 临时结果
   * @param sign 标记
   * @param rCards 剩余牌
   * @param curr 当前牌
   * @param remainCard 剩余牌数
   */
  private static void match(List<List<Integer>> tempRes, List<Boolean> sign, List<Integer> rCards, int curr, int remainCard) {
    if (rCards.isEmpty() || remainCard == 0) {
      if (tempRes.stream().filter(tc -> tc.size() == 2).filter(tc -> 
        tc.get(0) + 1 == tc.get(1) || tc.get(0) % 10 == 1 && tc.get(1) % 10 == 6 || tc.get(0) + 10 == tc.get(1)).count() >= 1L) {
        return;
      }
      if (tempRes.stream().mapToLong(tc -> tc.size()).sum() != rCards.size()) {
        return;
      }
      results.add(deepCopy(tempRes)); // 深拷贝 tempRes 并添加到 results
      System.out.println("temp results: " + results.get(results.size() - 1));
      return;
    } 
    if (Objects.isNull(tempRes)) {
      tempRes = new ArrayList<>(); 
    } else {
      boolean cutted = tempRes.stream().filter(tc -> tc.size() == 2).filter(tc -> 
        tc.get(0) + 1 == tc.get(1) || tc.get(0) % 10 == 1 && tc.get(1) % 10 == 6 || tc.get(0) + 10 == tc.get(1)).count() > 1L;
      if (cutted) { // 剪枝
        return;
      }
    }
    if (Objects.isNull(sign)) {
      sign = new ArrayList<>(Collections.nCopies(rCards.size(), false));
    }
    curr = curr % rCards.size();
    if (sign.get(curr)) {
      match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard);
      return;
    }
    int currCard = rCards.get(curr);
    for (List<Integer> combination : tempRes) {
      if (combination.size() >= 3) {
        continue;
      }
      if (combination.get(combination.size() - 1) == currCard) { // 对子 或 小带大后两张大牌
        if (combination.size() == 1 || combination.get(0) + 10 == combination.get(1)) {
          combination.add(currCard);
          match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
          combination.remove(combination.size() - 1); // 回溯
        }
      }
      if (combination.get(combination.size() - 1) + 1 == currCard) { // 普通顺子
        if (combination.size() == 1 || combination.get(0) + 1 == combination.get(1)) {
          combination.add(currCard);
          match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
          combination.remove(combination.size() - 1); // 回溯  
        }
      }
      if (combination.size() == 2 && combination.get(0) == combination.get(1) && combination.get(combination.size() - 1) + 10 == currCard) { // 小带大 
        combination.add(currCard);
        match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
        combination.remove(combination.size() - 1); // 回溯
      }
      if (combination.size() == 1 && combination.get(0) + 10 == currCard) { // 大带小
        combination.add(currCard);
        match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
        combination.remove(combination.size() - 1); // 回溯
      }
      if ((combination.size() == 1 && combination.get(0) % 10 == 1
          && currCard % 10 == 6 && currCard - combination.get(0) < 10)
          || (combination.size() == 2 && combination.get(0) % 10 == 1 && combination.get(1) % 10 == 6
              && currCard % 10 == 9 && currCard - combination.get(1) < 10)) { // 二七十
        combination.add(currCard);
        match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
        combination.remove(combination.size() - 1); // 回溯
      }
    }
    tempRes.add(new ArrayList<Integer>() {
      {
        add(currCard);
      }
    }); // 新的组合(单张)
    match(deepCopy(tempRes), new ArrayList<Boolean>(sign), rCards, curr + 1, remainCard - 1);
    tempRes.remove(tempRes.size() - 1); // 回溯
  }

  /**
   * 深拷贝 tempRes
   */
  private static List<List<Integer>> deepCopy(List<List<Integer>> tempRes) {
    List<List<Integer>> newTempRes = new ArrayList<>();
    for (List<Integer> subList : tempRes) {
      newTempRes.add(new ArrayList<>(subList));
    }
    return newTempRes;
  }
}