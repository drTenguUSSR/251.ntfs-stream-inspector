package mil.teng251.codesnippets.simple;

import com.google.common.base.Strings;
import lombok.Value;
import lombok.val;
import mil.teng251.codesnippets.SnipExec;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * for IDE run: add VM options:
 * VM options:
 *  -Djava.io.tmpdir=tmpFolder -Dlog4j2.configurationFile=config/log4j2.xml
 * run argument:
 *  run --args="-snippetName=account-check"
 */
public class ExecCorrespondentAccountCheckDigit implements SnipExec {
    private static final Logger log = LoggerFactory.getLogger(ExecCorrespondentAccountCheckDigit.class);
    private static final int[] BASE_713 = {7, 1, 3};

    private static String doKeyCalc(String bik, String account) {

        if (Strings.isNullOrEmpty(bik) || bik.length() != 9) {
            throw new IllegalArgumentException("BIK is null/empty or wrong format. BIK.len must be 9. got=[" + bik + "]");
        }
        if (Strings.isNullOrEmpty(account) || account.length() != 20) {
            throw new IllegalArgumentException("account is null/empty or wrong format. BIK.len must be 20. got=[" + bik + "]");
        }

        String bikSub = "0" + bik.substring(4, 6); //разряды 5 и 6.
        String fullNumber = bikSub + replaceAccountKeycode(account, "0");
        String controlKeyA = account.substring(8, 9); //разряд 9
        log.debug("doKeyCalc: bikSub={} controlKey={} fullNumber=!{}!", bikSub, controlKeyA, fullNumber);
        int checkSum = 0;
        for (int i1 = 0; i1 < fullNumber.length(); i1++) {
            int dat = Integer.parseInt(fullNumber.substring(i1, i1 + 1));
            checkSum += dat * BASE_713[i1 % 3];
        }
        int controlKeyB = ((checkSum % 10) * 3) % 10;
        log.debug("done. checksum={} controlKeyB={}", checkSum, controlKeyB);
        return String.valueOf(controlKeyB);
    }

    private static boolean doValidate(String bik, String account) {

        if (Strings.isNullOrEmpty(bik) || bik.length() != 9) {
            throw new IllegalArgumentException("BIK is null/empty or wrong format. BIK.len must be 9. got=[" + bik + "]");
        }
        if (Strings.isNullOrEmpty(account) || account.length() != 20) {
            throw new IllegalArgumentException("account is null/empty or wrong format. BIK.len must be 20. got=[" + bik + "]");
        }

        String bikSub = "0" + bik.substring(4, 6); //разряды 5 и 6.
        String fullNumber = bikSub + account;
        String controlKey = account.substring(8, 9); //разряд 9
        log.debug("doValidate: bikSub={} controlKey={} fullNumber=!{}!", bikSub, controlKey, fullNumber);
        int checkSum = 0;
        for (int i1 = 0; i1 < fullNumber.length(); i1++) {
            int dat = Integer.parseInt(fullNumber.substring(i1, i1 + 1));
            checkSum += dat * BASE_713[i1 % 3];
        }
        boolean isValid = checkSum % 10 == 0;
        log.debug("done. checksum={} isValid={}", checkSum, isValid);
        return isValid;
    }

    static String replaceAccountKeycode(String account, String newKey) {
        return account.substring(0, 8) + newKey + account.substring(9);
    }

    //using:
    //https://normativ.kontur.ru/document?moduleId=1&documentId=24444
    //https://uralsib.ru/business/articles/rasshifrovka-raschetnogo-scheta
    @Override
    public void execute(CommandLine commandLine) {
        log.debug("BankAccountNumberValidator - beg/2");
        //БИК 044525491 принадлежит банку КУ ООО ПИР Банк - ГК "АСВ"

        List<FullAccountTestInfo> testData = Arrays.asList(
                //https://keysystems.ru/files/fo/arm_budjet/show_docum/BKS/onlinehelp/index.html?ro_kr_algor_klyuch_korr_schet.htm
                //Коррсчет 30101810900000000746 .  Счет открыт в РКЦ, имеющем БИК 049805000. (9) must-fail
                //проверки https://www.iban.ru/proverka-scheta
                new FullAccountTestInfo("049805000", "30101810900000000746", false, "X")
                , new FullAccountTestInfo("049805000", "30101810800000000746", true, "8")
                , new FullAccountTestInfo("044525491", "40817810610000043893", true, "0")
                , new FullAccountTestInfo("044525491", "40817810610000043894", false, "0")
                // https://normativ.kontur.ru/document?moduleId=1&documentId=24444
                , new FullAccountTestInfo("040305000", "40102810100000010001", true, "X")
        );

        for (val testVal : testData) {
            log.debug("---------- test={}", testVal);
            if (doValidate(testVal.bik, testVal.account)) {
                log.debug("bik={} account={} valid code", testVal.bik, testVal.account);
                if (!testVal.resValid) {
                    throw new IllegalStateException("check must be valid");
                }
            } else {
                if (!"X".equals(testVal.resKey)) {
                    String key2 = doKeyCalc(testVal.bik, testVal.account);
                    log.debug("bik={} account={} INvalid code. recalcKey={}. mustResult={}"
                            , testVal.bik, testVal.account, key2, replaceAccountKeycode(testVal.account, key2));
                    if (testVal.resValid) {
                        throw new IllegalStateException("check must NOT be valid");
                    }
                }
            }
        }


        log.debug("BankAccountNumberValidator - end");
    }

    @Value
    static class FullAccountTestInfo {
        String bik;
        String account;
        boolean resValid;
        String resKey;
    }
}
