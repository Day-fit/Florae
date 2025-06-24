import { reqMeta, reqUnits } from '../util/snippets-values.jsx';

export default function PlantCard({
  primaryPhoto,
  speciesName,
  requirements,
  guestName,
  selection = 'optimal',
  onSelectionChange,
}) {
  const imgSrc = `data:image/png;base64,${primaryPhoto}`;

  function handleOptionChange(e) {
    e.stopPropagation();

    if (onSelectionChange) {
      onSelectionChange(e.target.value);
    }
  }

  function renderRequirements() {
    if (selection === 'optimal') {
      return [
        {
          label: 'Env. Humidity',
          value: (requirements.max_env_humid + requirements.min_env_humid) / 2,
        },
        {
          label: 'Light (lux)',
          value: (requirements.max_light_lux + requirements.min_light_lux) / 2,
        },
        {
          label: 'Soil Moisture',
          value: (requirements.max_soil_moist + requirements.min_soil_moist) / 2,
        },
        { label: 'Temperature', value: (requirements.max_temp + requirements.min_temp) / 2 },
      ];
    } else if (selection === 'min') {
      return [
        { label: 'Env. Humidity', value: requirements.min_env_humid },
        { label: 'Light (lux)', value: requirements.min_light_lux },
        { label: 'Soil Moisture', value: requirements.min_soil_moist },
        { label: 'Temperature', value: requirements.min_temp },
      ];
    } else {
      // max
      return [
        { label: 'Env. Humidity', value: requirements.max_env_humid },
        { label: 'Light (lux)', value: requirements.max_light_lux },
        { label: 'Soil Moisture', value: requirements.max_soil_moist },
        { label: 'Temperature', value: requirements.max_temp },
      ];
    }
  }

  return (
    <div className="w-full max-w-[340px] min-w-[220px] sm:w-[22vw] sm:max-w-[320px] sm:min-w-[220px] border-stone-400 rounded-2xl border-2 flex flex-col items-center justify-center min-h-150 bg-white shadow-lg mx-auto my-4 p-4 transition-transform duration-200 hover:scale-105">
      {/* Guest name centered */}
      <div className="flex justify-center mt-3 mb-2 w-full">
        <h1 className="text-green-700 text-xl sm:text-2xl font-bold text-center w-full break-words">
          {guestName}
        </h1>
      </div>
      {/* Image centered */}
      <div className="w-full h-36 sm:h-40 bg-stone-200 rounded-2xl mb-4 overflow-hidden flex items-center justify-center">
        <img src={imgSrc} alt={speciesName} className="object-cover w-full h-full" />
      </div>
      {/* Species name centered */}
      <div className="flex justify-center font-bold w-full mb-2">
        <h1 className="text-center w-full break-words text-base sm:text-lg">{speciesName}</h1>
      </div>
      {/* Select centered */}
      <div className="flex justify-center w-full mb-2">
        <select
          className="border w-[70%] sm:w-[60%] border-stone-400 rounded-lg px-2 py-2 text-base font-medium focus:outline-none focus:ring-2 focus:ring-green-400 bg-green-50 text-green-700 transition hover:bg-green-10"
          value={selection}
          onChange={handleOptionChange}
          onClick={(e) => e.stopPropagation()}
        >
          <option value="optimal">Optimal</option>
          <option value="max">Max</option>
          <option value="min">Min</option>
        </select>
      </div>
      {/* Requirements grid, centered */}
      <div className="flex flex-col items-center gap-2 w-full">
        {(() => {
          const requirements = renderRequirements();
          const rows = [];
          for (let i = 0; i < requirements.length; i += 2) {
            rows.push(requirements.slice(i, i + 2));
          }
          return rows.map((row, rowIdx) => (
            <div className="flex flex-row justify-center gap-2 sm:gap-4 w-full" key={rowIdx}>
              {row.map((req, idx) => {
                const realIdx = rowIdx * 2 + idx;
                return (
                  <div
                    key={realIdx}
                    className={`flex flex-col items-center justify-center h-[120px] sm:h-[140px] w-[45%] sm:w-[40%] md:w-[35%] rounded-xl shadow-lg border-2 ${reqMeta[realIdx].color} mx-auto my-2`}
                  >
                    <div className="flex items-center justify-center pt-4">
                      {reqMeta[realIdx].icon}
                    </div>
                    <div className="flex flex-col xs:flex-row items-center justify-center break-words w-full px-2">
                      <span className="text-base sm:text-lg md:text-xl font-bold text-center truncate max-w-full">
                        {req.value}
                        {String(req.value).length <= 5 ? reqUnits[realIdx] : ''}
                      </span>
                      {String(req.value).length > 5 && (
                        <span className="text-base sm:text-lg md:text-xl font-bold text-center ml-0 xs:ml-1 truncate max-w-full">
                          {reqUnits[realIdx]}
                        </span>
                      )}
                    </div>
                    <div className={`pb-3 pt-2 text-xs font-medium ${reqMeta[realIdx].labelColor}`}>
                      {req.label}
                    </div>
                  </div>
                );
              })}
            </div>
          ));
        })()}
      </div>
    </div>
  );
}
