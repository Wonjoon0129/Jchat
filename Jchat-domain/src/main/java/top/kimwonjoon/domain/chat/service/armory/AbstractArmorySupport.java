package top.kimwonjoon.domain.chat.service.armory;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.chat.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.chat.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName Abstract
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 10:27
 * @Version 1.0
 */
@Service
public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<Integer,DefaultArmoryStrategyFactory.DynamicContext,String> {

    private final Logger log = LoggerFactory.getLogger(AbstractArmorySupport.class);

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Resource
    protected IAgentRepository repository;

    @Override
    protected void multiThread(Integer avatarId, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
    }

    protected String beanName(Long id) {
        // 缺省的
        return "default";
    }

    /**
     * 通用的Bean注册方法
     *
     * @param beanName  Bean名称
     * @param beanClass Bean类型
     * @param <T>       Bean类型
     */
    public synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        // 注册Bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        // 如果Bean已存在，先移除
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }

        // 注册新的Bean
        beanFactory.registerBeanDefinition(beanName, beanDefinition);

        log.info("成功注册Bean: {}", beanName);
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

}
