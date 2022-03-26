package wang.seamas;

public interface ServiceResolver {

    default DropDownInterface resolve(Class<? extends DropDownInterface> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
