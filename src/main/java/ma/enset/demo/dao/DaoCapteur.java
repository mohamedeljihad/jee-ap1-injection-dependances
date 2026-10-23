package ma.enset.demo.dao;

import ma.enset.dao.IDao;
import ma.enset.framework.annotations.Component;

@Component("daoCapteur")
public class DaoCapteur implements IDao {
    @Override
    public double getData() {
        System.out.println("  [DaoCapteur] Version capteurs");
        return 12;
    }
}
