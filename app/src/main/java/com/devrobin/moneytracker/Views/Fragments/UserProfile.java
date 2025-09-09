package com.devrobin.moneytracker.Views.Fragments;

import android.content.Intent;
import android.content.pm.CrossProfileApps;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.devrobin.moneytracker.Views.activity.AboutUs;
import com.devrobin.moneytracker.Views.activity.AccountManagement;
import com.devrobin.moneytracker.Views.activity.BudgetActivity;
import com.devrobin.moneytracker.Views.activity.CategorySettingsActivity;
import com.devrobin.moneytracker.Views.activity.ChangePasswordActivity;
import com.devrobin.moneytracker.Views.activity.CurrencySettingsActivity;
import com.devrobin.moneytracker.Views.activity.DeleteAllDataActivity;
import com.devrobin.moneytracker.Views.activity.ExportSettingsActivity;
import com.devrobin.moneytracker.Views.activity.FontSizeActivity;
import com.devrobin.moneytracker.Views.activity.LanguageSettingsActivity;
import com.devrobin.moneytracker.Views.activity.NotificationSettingsActivity;
import com.devrobin.moneytracker.Views.activity.PrivacyAndPolicy;
import com.devrobin.moneytracker.Views.activity.ProfileSettingsActivity;
import com.devrobin.moneytracker.Views.activity.ReminderActivity;
import com.devrobin.moneytracker.Views.activity.TermsAndConditions;
import com.devrobin.moneytracker.Views.activity.UsersFeedBack;
import com.devrobin.moneytracker.databinding.FragmentUserProfileBinding;


public class UserProfile extends Fragment {

    private FragmentUserProfileBinding profileBinding;

    public UserProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        profileBinding = FragmentUserProfileBinding.inflate(inflater, container, false);


        setUpClickListener();

        return profileBinding.getRoot();
    }

    private void setUpClickListener() {

        profileBinding.ProfileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileSettingsActivity.class);
                startActivity(intent);
            }
        });

        profileBinding.accountContainerr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), AccountManagement.class);
                startActivity(intent);

            }
        });

        profileBinding.categoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), CategorySettingsActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.currencyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CurrencySettingsActivity.class);
                startActivity(intent);
            }
        });

        profileBinding.notificationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), NotificationSettingsActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.reminderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ReminderActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.budgetContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), BudgetActivity.class);
                startActivity(intent);
            }
        });

        profileBinding.exportContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), ExportSettingsActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.PasswordContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.languageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), LanguageSettingsActivity.class);
                startActivity(intent);
            }
        });

        profileBinding.themesContainerr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Click SuccessFull", Toast.LENGTH_SHORT).show();
            }
        });

        profileBinding.fontContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), FontSizeActivity.class);
                startActivity(intent);

            }
        });

        profileBinding.deleteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DeleteAllDataActivity.class);
                startActivity(intent);
            }
        });

        profileBinding.securityContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PrivacyAndPolicy.class);
                startActivity(intent);
            }
        });

        profileBinding.termsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TermsAndConditions.class);
                startActivity(intent);
            }
        });

        profileBinding.aboutUsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AboutUs.class);
                startActivity(intent);
            }
        });

        profileBinding.feedBackContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), UsersFeedBack.class);
                startActivity(intent);

            }
        });

    }
}