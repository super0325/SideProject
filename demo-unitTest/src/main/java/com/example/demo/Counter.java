package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class Counter {

    /**
     * 獲取指定範圍內每個數字的特殊單詞串接結果。
     *
     * @param input 要檢查的數字範圍的上限
     * @return 特殊單詞串接結果
     */
    public String getCount(int input) {
        StringBuilder result = new StringBuilder();

        // 定義需要檢查的特殊數字列表
        List<Integer> numbersToCheck = List.of(3, 5);

        for (int i = 1; i <= input; i++) {
        	final int currentNumber = i;  // 將循環變量 i 的值複製到 final 變量 currentNumber 中，以確保在 lambda 表達式中能夠正確訪問它

            // 使用流來檢查數字是否滿足特定條件
            List<String> specialWords = numbersToCheck.stream()
                    .filter(number -> currentNumber % number == 0)  // 檢查是否能被特殊數字整除
                    .map(this::getWordForNumber)                    // 將符合條件的數字映射為相應的特殊單詞
                    .collect(Collectors.toList());                  // 收集特殊單詞到列表中

            if (!specialWords.isEmpty()) {
                // 如果有特殊單詞，將它們串接為一個字串並添加到結果中
                String joinedWords = specialWords.stream().map(String::trim).collect(Collectors.joining());
                result.append(" ").append(joinedWords);
            } else {
                // 如果沒有特殊單詞，將當前數字添加到結果中
                result.append(" ").append(i);
            }
        }

        return result.toString().trim(); // 返回去除頭尾空白後的結果字串
    }

    /**
     * 根據給定的數字返回相應的特殊單詞。
     *
     * @param number 要映射的數字
     * @return 相應的特殊單詞，如果沒有對應的特殊單詞則返回空字串
     */
    private String getWordForNumber(int number) {
        // 根據數字返回相應的單詞
        switch (number) {
            case 3:
                return "clap";
            case 5:
                return "ha";
            default:
                return ""; // 默認情況下返回空字串
        }
    }

    public static void main(String args[]) {
        Counter counter = new Counter();
        System.out.println(counter.getCount(10));
        System.out.println(counter.getCount(15));
        System.out.println(counter.getCount(0));
        System.out.println(counter.getCount(-1));
    }
}
