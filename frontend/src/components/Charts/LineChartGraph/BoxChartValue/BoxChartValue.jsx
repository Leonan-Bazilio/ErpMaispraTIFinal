import React from 'react'
import './BoxChartValue.css'

function BoxChartValue({title, isCredit, value}) {

    const formatarRealSeparado = (valor) => {
        const formatado = (valor / 100).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        const [antesDaVirgula, depoisDaVirgula] = formatado.split(",");
        return { antesDaVirgula, depoisDaVirgula };
    }
const valor = formatarRealSeparado(value);

  return (
    <div className='BoxChartValue'>
      <h2>{title}</h2>
    
      <div className="valuebox" style={{ backgroundColor: isCredit ? 'Green' : 'red' }}>
        <p>,{valor.depoisDaVirgula}</p>    
        
        <span>
            {valor.antesDaVirgula}
        </span>
        
        <p>R$</p>
    </div>

    </div>
  )
}

export default BoxChartValue