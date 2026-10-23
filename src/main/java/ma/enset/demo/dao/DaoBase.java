package ma.enset.demo.dao;

import ma.enset.dao.IDao;
import ma.enset.framework.annotations.Component;

@Component("daoBase")
public class DaoBase implements IDao {
    @Override
    public double getData() {
        System.out.println("  [DaoBase] Version base de données");
        return 34;
    }
}
