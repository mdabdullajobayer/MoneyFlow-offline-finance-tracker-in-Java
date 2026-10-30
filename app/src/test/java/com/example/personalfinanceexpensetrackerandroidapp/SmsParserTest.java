package com.example.personalfinanceexpensetrackerandroidapp;

import com.example.personalfinanceexpensetrackerandroidapp.util.SmsParser;

import org.junit.Test;
import static org.junit.Assert.*;

public class SmsParserTest {

    @Test
    public void testParseReceivedSms() {
        String body = "You have received Tk 1,500.50 from 01700000000. TrxID ABC123XYZ.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("IN", result.transactionType);
        assertEquals(1500.50, result.amount, 0.001);
        assertEquals("ABC123XYZ", result.reference);
    }

    @Test
    public void testParsePaymentSms() {
        String body = "Payment of BDT 250.00 successful. Ref: 987654321.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("OUT", result.transactionType);
        assertEquals(250.0, result.amount, 0.001);
        assertEquals("987654321", result.reference);
    }

    @Test
    public void testParseLoanTakingSms() {
        String body = "Loan of BDT 50,000 received in your account. Ref: LOAN999.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("IN", result.transactionType);
        assertEquals(50000.0, result.amount, 0.001);
        assertEquals("LOAN999", result.reference);
    }

    @Test
    public void testParseLoanRepaymentSms() {
        String body = "Loan repayment of Tk 5,000 successful. Ref: REP123.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("OUT", result.transactionType);
        assertEquals(5000.0, result.amount, 0.001);
        assertEquals("REP123", result.reference);
    }

    @Test
    public void testParseTransferSms() {
        String body = "Fund transfer of Tk 10,000 successful. Ref: TRF456.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("TRANSFER", result.transactionType);
        assertEquals(10000.0, result.amount, 0.001);
        assertEquals("TRF456", result.reference);
    }

    @Test
    public void testParseAccountToAccountTransferSms() {
        String body = "Fund transfer of Tk 12,500 from Prime Bank Account to bKash Personal successful. Ref: TRF789.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("TRANSFER", result.transactionType);
        assertEquals(12500.0, result.amount, 0.001);
        assertEquals("TRF789", result.reference);
        assertEquals("Prime Bank Account", result.sourceAccount);
        assertEquals("bKash Personal", result.destinationAccount);
    }

    @Test
    public void testParseLoanTakingCategorySms() {
        String body = "Loan of BDT 75,000 received and credited. Ref: LOANTAK123.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("IN", result.transactionType);
        assertEquals("Loan Taking", result.categorySuggestion);
        assertEquals(75000.0, result.amount, 0.001);
    }

    @Test
    public void testParseLoanRepaymentCategorySms() {
        String body = "Loan repayment of Tk 4,500 deducted. Ref: LOANREP456.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertTrue(result.success);
        assertEquals("OUT", result.transactionType);
        assertEquals("Loan Repayment", result.categorySuggestion);
        assertEquals(4500.0, result.amount, 0.001);
    }

    @Test
    public void testParseInvalidSms() {
        String body = "Random notification without amount.";
        SmsParser.ParsedResult result = SmsParser.parse(body);

        assertFalse(result.success);
    }
}
