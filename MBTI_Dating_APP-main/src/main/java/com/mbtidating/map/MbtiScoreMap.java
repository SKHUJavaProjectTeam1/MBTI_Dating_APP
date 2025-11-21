package com.mbtidating.map;

import java.util.*;

public class MbtiScoreMap {

    public static final Map<String, Map<String, Integer>> TABLE = Map.ofEntries(

// ========== INFP ==========
            Map.entry("INFP", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 4),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 4),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 0),
                    Map.entry("ESFP", 0),
                    Map.entry("ISTP", 0),
                    Map.entry("ESTP", 0),
                    Map.entry("ISFJ", 0),
                    Map.entry("ESFJ", 0),
                    Map.entry("ISTJ", 0),
                    Map.entry("ESTJ", 0)
            )),

// ========== ENFP ==========
            Map.entry("ENFP", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 4),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 4),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 0),
                    Map.entry("ESFP", 0),
                    Map.entry("ISTP", 0),
                    Map.entry("ESTP", 0),
                    Map.entry("ISFJ", 0),
                    Map.entry("ESFJ", 0),
                    Map.entry("ISTJ", 0),
                    Map.entry("ESTJ", 0)
            )),

// ========== INFJ ==========
            Map.entry("INFJ", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 4),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 4),
                    Map.entry("ISFP", 0),
                    Map.entry("ESFP", 0),
                    Map.entry("ISTP", 0),
                    Map.entry("ESTP", 0),
                    Map.entry("ISFJ", 0),
                    Map.entry("ESFJ", 0),
                    Map.entry("ISTJ", 0),
                    Map.entry("ESTJ", 0)
            )),

// ========== ENFJ ==========
            Map.entry("ENFJ", Map.ofEntries(
                    Map.entry("INFP", 4),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 4),
                    Map.entry("ESFP", 0),
                    Map.entry("ISTP", 0),
                    Map.entry("ESTP", 0),
                    Map.entry("ISFJ", 0),
                    Map.entry("ESFJ", 0),
                    Map.entry("ISTJ", 0),
                    Map.entry("ESTJ", 0)
            )),

// ========== INTJ ==========
            Map.entry("INTJ", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 4),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 4),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 1),
                    Map.entry("ESFJ", 1),
                    Map.entry("ISTJ", 1),
                    Map.entry("ESTJ", 1)
            )),

// ========== ENTJ ==========
            Map.entry("ENTJ", Map.ofEntries(
                    Map.entry("INFP", 4),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 4),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 1),
                    Map.entry("ESFJ", 1),
                    Map.entry("ISTJ", 1),
                    Map.entry("ESTJ", 1)
            )),

// ========== INTP ==========
            Map.entry("INTP", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 3),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 4),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 1),
                    Map.entry("ESFJ", 1),
                    Map.entry("ISTJ", 1),
                    Map.entry("ESTJ", 4)
            )),

// ========== ENTP ==========
            Map.entry("ENTP", Map.ofEntries(
                    Map.entry("INFP", 3),
                    Map.entry("ENFP", 3),
                    Map.entry("INFJ", 4),
                    Map.entry("ENFJ", 3),
                    Map.entry("INTJ", 3),
                    Map.entry("ENTJ", 3),
                    Map.entry("INTP", 3),
                    Map.entry("ENTP", 3),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 1),
                    Map.entry("ESFJ", 1),
                    Map.entry("ISTJ", 1),
                    Map.entry("ESTJ", 1)
            )),

// ========== ISFP ==========
            Map.entry("ISFP", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 4),
                    Map.entry("INTJ", 2),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 2),
                    Map.entry("ENTP", 2),
                    Map.entry("ISFP", 1),
                    Map.entry("ESFP", 1),
                    Map.entry("ISTP", 1),
                    Map.entry("ESTP", 1),
                    Map.entry("ISFJ", 2),
                    Map.entry("ESFJ", 4),
                    Map.entry("ISTJ", 2),
                    Map.entry("ESTJ", 4)
            )),

// ========== ESFP ==========
            Map.entry("ESFP", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 2),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 2),
                    Map.entry("ENTP", 2),
                    Map.entry("ISFP", 1),
                    Map.entry("ESFP", 1),
                    Map.entry("ISTP", 1),
                    Map.entry("ESTP", 1),
                    Map.entry("ISFJ", 4),
                    Map.entry("ESFJ", 2),
                    Map.entry("ISTJ", 4),
                    Map.entry("ESTJ", 2)
            )),

// ========== ISTP ==========
            Map.entry("ISTP", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 2),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 2),
                    Map.entry("ENTP", 2),
                    Map.entry("ISFP", 1),
                    Map.entry("ESFP", 1),
                    Map.entry("ISTP", 1),
                    Map.entry("ESTP", 1),
                    Map.entry("ISFJ", 2),
                    Map.entry("ESFJ", 4),
                    Map.entry("ISTJ", 2),
                    Map.entry("ESTJ", 4)
            )),

// ========== ESTP ==========
            Map.entry("ESTP", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 2),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 2),
                    Map.entry("ENTP", 2),
                    Map.entry("ISFP", 1),
                    Map.entry("ESFP", 1),
                    Map.entry("ISTP", 1),
                    Map.entry("ESTP", 1),
                    Map.entry("ISFJ", 4),
                    Map.entry("ESFJ", 2),
                    Map.entry("ISTJ", 2),
                    Map.entry("ESTJ", 2)
            )),

// ========== ISFJ ==========
            Map.entry("ISFJ", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 1),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 1),
                    Map.entry("ENTP", 1),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 4),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 4),
                    Map.entry("ISFJ", 3),
                    Map.entry("ESFJ", 3),
                    Map.entry("ISTJ", 3),
                    Map.entry("ESTJ", 3)
            )),

// ========== ESFJ ==========
            Map.entry("ESFJ", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 1),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 1),
                    Map.entry("ENTP", 1),
                    Map.entry("ISFP", 4),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 4),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 3),
                    Map.entry("ESFJ", 3),
                    Map.entry("ISTJ", 3),
                    Map.entry("ESTJ", 3)
            )),

// ========== ISTJ ==========
            Map.entry("ISTJ", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 1),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 1),
                    Map.entry("ENTP", 1),
                    Map.entry("ISFP", 2),
                    Map.entry("ESFP", 4),
                    Map.entry("ISTP", 2),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 3),
                    Map.entry("ESFJ", 3),
                    Map.entry("ISTJ", 3),
                    Map.entry("ESTJ", 3)
            )),

// ========== ESTJ ==========
            Map.entry("ESTJ", Map.ofEntries(
                    Map.entry("INFP", 0),
                    Map.entry("ENFP", 0),
                    Map.entry("INFJ", 0),
                    Map.entry("ENFJ", 0),
                    Map.entry("INTJ", 1),
                    Map.entry("ENTJ", 2),
                    Map.entry("INTP", 4),
                    Map.entry("ENTP", 1),
                    Map.entry("ISFP", 4),
                    Map.entry("ESFP", 2),
                    Map.entry("ISTP", 4),
                    Map.entry("ESTP", 2),
                    Map.entry("ISFJ", 3),
                    Map.entry("ESFJ", 3),
                    Map.entry("ISTJ", 3),
                    Map.entry("ESTJ", 3)
            ))
    );

    private MbtiScoreMap() {}
}