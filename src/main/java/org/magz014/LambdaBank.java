package org.magz014;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class LambdaBank implements RequestHandler<BankRequest, BankResponse> {

    @Override
    public BankResponse handleRequest(BankRequest bankRequest, Context context) {

        // Para cálculos exactos
        MathContext mathContext = MathContext.DECIMAL128;

        // Redondeo estilo financiero (banquero)
        BigDecimal amount = bankRequest.getAmount().setScale(2, RoundingMode.HALF_UP);

        // Convertimos la tasa mensual de porcentaje a decimal (ej. 1.5% -> 0.015)
        BigDecimal monthlyRate = bankRequest.getRate()
                .setScale(2, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100),mathContext);

        // Tasa ajustada si el cliente tiene cuenta de ahorro (-0.2 puntos porcentuales)
        BigDecimal monthlyRateWithAccount = bankRequest.getRate()
                .subtract(BigDecimal.valueOf(0.2),mathContext)
                .setScale(2,RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), mathContext);

        Integer term = bankRequest.getTerm();

        BigDecimal monthlyPayment  = this.calculateQuota(amount, monthlyRate, term, mathContext);
        BigDecimal monthlyPaymentWhitAccount = this.calculateQuota(amount, monthlyRateWithAccount, term, mathContext);

        BankResponse bankResponse = new BankResponse();
        bankResponse.setQuota(monthlyPayment);
        bankResponse.setRate(monthlyRate);
        bankResponse.setTerm(term);
        bankResponse.setQuotaWithAccount(monthlyPaymentWhitAccount);
        bankResponse.setRateWithAccount(monthlyRateWithAccount);
        bankResponse.setTermWithAccount(term);


        return bankResponse;
    }

    public BigDecimal calculateQuota (BigDecimal amount, BigDecimal rate, Integer term, MathContext mathContext){

        /**
         * P = Monto del préstamo
         * i = Tasa de interés mensual
         * n = Plazo del crédito en meses
         *
         * Cuota mensual = (P * i) / (1 - (1 + i) ^ (-n))
         */
        // Calcular (1 + i)
        BigDecimal onePlusRate = rate.add(BigDecimal.ONE,mathContext);

        //Calcular [onePlusRate ^ n] para obtener el reciproco
        BigDecimal onePlusRateToN = onePlusRate.pow(term, mathContext);

        // Reciproco para poder tener el resultado del exponente negativo
        BigDecimal onePlusRateToNegativeN = BigDecimal.ONE.divide(onePlusRateToN, mathContext);

        //Calcular cuota mensual normal -> (P * i)
        BigDecimal numerator = amount.multiply(rate, mathContext);

        /*
        * Para hacer los calculos de un exponente negativo usaremos el reciproco
        *  (1 + i) ^ (-n) == 1/(1 + i) ^ (-n)
         */
        // Calculamos (1 + i)^(-n) como el recíproco de (1 + i)^n
        BigDecimal denominator = BigDecimal.ONE.subtract(onePlusRateToNegativeN, mathContext);

        // Hacer la división de numerator/denominator con mathContext para cálculos exactos
        BigDecimal monthlyPayment = numerator.divide(denominator, mathContext);

        // Redondeo del resultado
        monthlyPayment = monthlyPayment.setScale(2, RoundingMode.HALF_UP);

        return monthlyPayment;
    }
}