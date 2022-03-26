package wang.seamas;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class ExcelToolTest {


    @Test
    public void test() {
        try (FileOutputStream outputStream = new FileOutputStream("target/aaa.xls")) {
            ExcelTool.writeExcelTemplate(Model.class, new ServiceResolver() {
                @Override
                public DropDownInterface resolve(Class<? extends DropDownInterface> clazz) {

                    return ServiceResolver.super.resolve(clazz);
                }
            }, outputStream);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}