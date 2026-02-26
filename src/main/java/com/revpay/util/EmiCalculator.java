package com.revpay.util;

public class EmiCalculator {

    public static double calculate(double principal, double annualRate, int months) {

        double monthlyRate = annualRate / 12 / 100;

        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, months))
                / (Math.pow(1 + monthlyRate, months) - 1);

        return Math.round(emi * 100.0) / 100.0;
    }
}