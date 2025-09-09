package com.devrobin.moneytracker.MVVM.Model;

public class BudgetVsActualData {
    private final String category;
    private final String budgetType;
    private final double budgetAmount;
    private final double spentAmount;

    public BudgetVsActualData(String category, String budgetType, double budgetAmount, double spentAmount) {
        this.category = category;
        this.budgetType = budgetType;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
    }

    // Getters
    public String getCategory() { return category; }
    public String getBudgetType() { return budgetType; }
    public double getBudgetAmount() { return budgetAmount; }
    public double getSpentAmount() { return spentAmount; }
    public double getRemainingAmount() { return budgetAmount - spentAmount; }

    public int getProgressPercentage() {
        try {
            return (int) ((spentAmount / budgetAmount) * 100);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getStatus() {
        try {
            if (spentAmount > budgetAmount) return "Over Budget";
            if (spentAmount > budgetAmount * 0.8) return "Near Limit";
            return "Within Budget";
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
