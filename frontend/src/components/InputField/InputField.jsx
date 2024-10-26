import "./InputField.css";

function InputField({
  label,
  type = "text",
  placeholder,
  name,
  value,
  onChange,
  required = true,
  onInvalid,
  idInput,
  classNameDiv = "",
}) {
  return (
    <div className={classNameDiv}>
      <label htmlFor={idInput} className="inputLabel">
        <span className="inputDescription">{label}</span>
        <input
          type={type}
          placeholder={placeholder}
          className="inputText"
          name={name}
          id={idInput}
          value={value}
          required={required}
          onInvalid={onInvalid}
          onChange={onChange}
        />
      </label>
    </div>
  );
}

export default InputField;