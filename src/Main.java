import java.util.*;

class Main {

  private static List<List<List<Integer>>> results = new ArrayList<>();

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    List<Integer> handCards = Arrays.asList(
        23, 24, 25, 16, 17, 18, 14, 15, 16, 12, 22, 11, 21, 24);
    handCards.sort(Integer::compare);
    System.out.println("sorted handCards: " + handCards);
    // in = (int) handCards.stream().filter(c -> c.equals(51)).count();
    match(null, handCards, 0, handCards.size());
    assert !results.isEmpty() : "no matched result";
    results.sort(new Comparator<List<List<Integer>>>() {
      @Override
      public int compare(List<List<Integer>> o1, List<List<Integer>> o2) {
        // return o1.stream().filter(tc -> tc.size() == 1).count() - o2.stream().filter(tc -> tc.size() == 1).count() >= 0L ? 1 : -1;
        long o1Count = o1.stream().filter(tc -> tc.size() == 1).count();
        long o2Count = o2.stream().filter(tc -> tc.size() == 1).count();
        return Long.compare(o1Count, o2Count);
      }
    });
    System.out.println("final result: " + results);
    List<List<Integer>> bestRes = results.get(0);
    System.out.println("best result: " + bestRes);
    List<Integer> shouldOut = bestRes.stream().filter(tc -> tc.size() == 1).findFirst().get();
    if (Objects.nonNull(shouldOut)) { // 存在散牌
      System.out.println("should out: " + shouldOut);
    } else { // 不存在散牌，有取舍地打
      // 优先拆普通顺子
      List<Integer> straight = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) + 1 == tc.get(1) && tc.get(0) % 10 != 1).findFirst().get();
      // 次优拆对子
      List<Integer> pair = bestRes.stream().filter(tc -> tc.size() == 2).findFirst().get();
      // 实在没有拆二带一
      List<Integer> twoWithOne = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) + 10 == tc.get(1) || tc.get(1) + 10 == tc.get(2)).findFirst().get();
      // 再没有就拆二七十和一二三
      List<Integer> twoWith70 = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) % 10 == 1 && tc.get(1) % 10 == 6 && tc.get(2) % 10 == 9).findFirst().get();
      List<Integer> threeWith123 = bestRes.stream().filter(tc -> tc.size() == 3).filter(tc -> tc.get(0) % 10 == 1 && tc.get(1) % 10 == 2 && tc.get(2) % 10 == 3).findFirst().get();
      if (Objects.nonNull(straight)) {
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
   * 
   * @param tempRes    临时结果
   * @param rCards     剩余牌
   * @param curr       当前下标
   * @para  剩余鬼
   * @param remainCard 剩余牌数
   */
  private static void match(List<List<Integer>> tempRes, List<Integer> rCards, int curr,
      int remainCard) {
    if (curr == rCards.size()) {
      results.add(deepCopy(tempRes)); // 深拷贝 tempRes 并添加到 results
      System.out.println("temp results: " + results.get(results.size() - 1));
      return;
    }
    if (rCards.size() == 0 || remainCard == 0) {
      return;
    }
    if (Objects.isNull(tempRes)) {
      tempRes = new ArrayList<>();
    } else {
      boolean cutted = tempRes.stream().filter(tc -> tc.size() == 2).filter(tc -> tc.get(0) + 1 == tc.get(1))
          .count() > 1L;
      if (cutted) { // 剪枝
        return;
      }
    }
    int currCard = rCards.get(curr);
    if (tempRes.isEmpty()) {
      tempRes.add(new ArrayList<>());
    }
    List<Integer> latestCombination = tempRes.get(tempRes.size() - 1);
    if (latestCombination.size() == 3) {
      tempRes.add(new ArrayList<>());
      latestCombination = tempRes.get(tempRes.size() - 1);
    }
    if (latestCombination.isEmpty()) {
      latestCombination.add(currCard);
      match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
      latestCombination.remove(latestCombination.size() - 1); // 回溯
    } else {
      if (latestCombination.get(latestCombination.size() - 1) == currCard) { // 对子 或 小带大后两张大牌
        if (latestCombination.size() == 1 || latestCombination.get(0) + 10 == latestCombination.get(1)) {
          latestCombination.add(currCard);
          match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
          latestCombination.remove(latestCombination.size() - 1); // 回溯
        }
      }
      if (latestCombination.get(latestCombination.size() - 1) + 1 == currCard) { // 普通顺子
        if (latestCombination.size() == 1 || latestCombination.get(0) + 1 == latestCombination.get(1)) {
          latestCombination.add(currCard);
          match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
          latestCombination.remove(latestCombination.size() - 1); // 回溯
        }
      }
      if (latestCombination.get(latestCombination.size() - 1) + 10 == currCard) { // 大带小
        latestCombination.add(currCard);
        match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
        latestCombination.remove(latestCombination.size() - 1); // 回溯
      }
      if ((latestCombination.size() == 1 && latestCombination.get(latestCombination.size() - 1) % 10 == 1
          && currCard % 10 == 6)
          || (latestCombination.size() == 2 && latestCombination.get(latestCombination.size() - 1) % 10 == 6
              && currCard % 10 == 9)) { // 二七十
        latestCombination.add(currCard);
        match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
        latestCombination.remove(latestCombination.size() - 1); // 回溯
      }
      tempRes.add(new ArrayList<Integer>() {
        {
          add(currCard);
        }
      }); // 新的组合(单张)
      match(deepCopy(tempRes), rCards, curr + 1, remainCard - 1);
      tempRes.remove(tempRes.size() - 1); // 回溯
    }
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