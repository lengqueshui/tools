package com.sq.tools.utils;

public class AmountChangeUtil {

    private static final String[] NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    private static final String[] UNITS = {"分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾",
            "佰", "仟", "兆", "拾", "佰", "仟"};

    private static long getLongDigit(long number) {
        if (number <= 0) {
            return 0;
        }

        int digit = 0;
        while (number > 0) {
            digit++;
            number = number / 10;
        }

        return digit;
    }

    public static String transition(double doubleMoney) {
        long number = (long) (doubleMoney * 100);
        if (number < 0) {
            return "负";
        }

        long signNum = getLongDigit(number);
        if (signNum == 0) {
            return "零元";
        }

        int numUnit;
        int numIndex = 0;
        boolean getZero = false;
        StringBuffer sb = new StringBuffer();
        int zeroSize = 0;
        while (true) {
            if (number <= 0) {
                break;
            }

            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    sb.insert(0, UNITS[6]);
                }

                if ((numIndex == 13) && (zeroSize >= 3)) {
                    sb.insert(0, UNITS[10]);
                }

                sb.insert(0, UNITS[numIndex]);
                sb.insert(0, NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (numIndex != 0 && numIndex != 1 && numIndex != 2
                        && numIndex != 6 && numIndex != 10 && numIndex != 14) {
                    if (!getZero) {
                        sb.insert(0, NUMBER[numUnit]);
                    }
                }

                if (numIndex == 2) {
                    if (number > 0) {
                        sb.insert(0, UNITS[numIndex]);
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.insert(0, UNITS[numIndex]);
                }
                getZero = true;
            }

            number = number / 10;
            numIndex++;
        }

        return sb.toString();
    }

}
