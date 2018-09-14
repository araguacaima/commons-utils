package com.araguacaima.commons.utils.filter;


import com.araguacaima.commons.utils.Constants;
import com.araguacaima.commons.utils.builder.SpecialParamSplitter;
import com.araguacaima.commons.utils.builder.SpecialParamSplitterBuilder;
import com.araguacaima.commons.utils.parser.ExtendableFiqlParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

public class RestQueryStringUtil {

    private final ClassLoader classLoader;

    private String packageBase;

    static {
        ExtendableFiqlParser.addOperator("=in=", ConditionType.CUSTOM);
        ExtendableFiqlParser.addOperator("=out=", ConditionType.CUSTOM);
    }

    public RestQueryStringUtil(ClassLoader classLoader, String packageBase) {
        this.classLoader = classLoader;
        this.packageBase = packageBase;
    }

    public RestQueryStringUtil(String packageBase) {
        this(RestQueryStringUtil.class.getClassLoader(), packageBase);
    }

    public String getPackageBase() {
        return packageBase;
    }

    public void setPackageBase(String packageBase) {
        this.packageBase = packageBase;
    }

    public <T> T createNewBeanAndFillItByExtractingFiqlFilter(String queryString, Class<T> dtoExtClass) throws IllegalArgumentException, IllegalAccessException, InstantiationException {

        ExtendableFiqlParser<T> parser = new ExtendableFiqlParser<T>(dtoExtClass, classLoader, packageBase);
        try {
            if (StringUtils.isNotBlank(queryString)) {
                Map<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> specialParams = SpecialParamSplitterBuilder.build(queryString);
                if (specialParams == null) {
                    throw new IllegalArgumentException("Incoming query params '" + queryString
                            + "' not meet the specification of the special parameters");
                }
                Collection<SpecialParamSplitter> specialParamSplitted = specialParams.get(Constants.SpecialQueryParams.FILTER);
                if (specialParamSplitted == null || specialParamSplitted.size() == 0) {
                    throw new IllegalArgumentException("Incoming query params '" + queryString
                            + "' does not have a valid '$filter' special parameter");
                }
                String filterParam = specialParamSplitted.iterator().next().getRightSideParam();
                SearchCondition<T> condition = parser.parse(filterParam, packageBase);
                //TODO AMM: Terminar de validar qué se hará con la expresión ya parseada y estructurada.
                //TODO AMM: Por lo pronto únicamente se está haciendo uso del bean resultante, pero cabe destacar que
                //TODO AMM: el mismo contendrá únicamente los valores asociados a la última porción parseada de cada
                //TODO AMM: atributo, por lo que si la expresión contiene varias veces un valor para cierto atributo
                //TODO AMM: los 'n-1' primeros se pierden, prevaleciendo valor únicamente para el último. Adicional a
                //TODO AMM: ésto si un tipo es abstracto (interfaz o clase hija especificada vía notación generic), o
                //TODO AMM: si es un arreglo tipado, se convervará la última implementación concreta disponible en la
                //TODO AMM: expresión. Esta situación es deseable corregir por cuanto el bean resultante no expresa
                //TODO AMM: completamente lo indicado en la expresión. De hecho es conveniente pensar en que éste
                //TODO AMM: método devuelva a lo sumo un arreglo de beans, y que cad auno se llene conforme vaya
                //TODO AMM: parseando cada porción de la expresión original.
                return (T) parser.getBean();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    public <T> T createNewBeanAndFillItByExtractingFiqlFilter(HttpServletRequest request, Class<T> dtoExtClass) throws IllegalArgumentException, InstantiationException, IllegalAccessException {
        return createNewBeanAndFillItByExtractingFiqlFilter(request.getQueryString(), dtoExtClass);
    }
}
